package com.yuyakaido.android.cardstackview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.yuyakaido.android.cardstackview.internal.CardContainerView;
import com.yuyakaido.android.cardstackview.internal.CardStackOption;
import com.yuyakaido.android.cardstackview.internal.CardStackState;
import com.yuyakaido.android.cardstackview.internal.SwipedItem;
import com.yuyakaido.android.cardstackview.internal.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class CardStackView extends FrameLayout {

    public interface CardEventListener {
        void onCardDragging(float percentX, float percentY);

        void onCardSwiped(SwipeDirection direction);

        void onCardReversed();

        void onCardMovedToOrigin();

        void onCardClicked(int index);
    }

    private CardStackOption option = new CardStackOption();
    private CardStackState state = new CardStackState();

    private BaseAdapter adapter = null;
    private Class<?> cardContainerViewClass = CardContainerView.class;
    private LinkedList<CardContainerView> containers = new LinkedList<>();
    private CardEventListener cardEventListener = null;
    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            boolean shouldReset = false;
            if (state.isPaginationReserved) {
                state.isPaginationReserved = false;
            } else {
                boolean isSameCount = state.lastCount == adapter.getCount();
                shouldReset = !isSameCount;
            }
            initialize(shouldReset);
            state.lastCount = adapter.getCount();
        }
    };
    private CardContainerView.ContainerEventListener containerEventListener = new CardContainerView.ContainerEventListener() {
        @Override
        public void onContainerDragging(float percentX, float percentY) {
            update(percentX, percentY);
        }

        @Override
        public void onContainerSwiped(Point point, SwipeDirection direction) {
            swipe(point, direction);
        }

        @Override
        public void onContainerMovedToOrigin() {
            initializeCardStackPosition();
            if (cardEventListener != null) {
                cardEventListener.onCardMovedToOrigin();
            }
        }

        @Override
        public void onContainerClicked() {
            if (cardEventListener != null) {
                cardEventListener.onCardClicked(state.topIndex);
            }
        }
    };

    public CardStackView(Context context) {
        this(context, null);
    }

    public CardStackView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardStackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardStackView);
        setVisibleCount(array.getInt(R.styleable.CardStackView_visibleCount, option.visibleCount));
        setSwipeThreshold(array.getFloat(R.styleable.CardStackView_swipeThreshold, option.swipeThreshold));
        setTranslationDiff(array.getFloat(R.styleable.CardStackView_translationDiff, option.translationDiff));
        setScaleDiff(array.getFloat(R.styleable.CardStackView_scaleDiff, option.scaleDiff));
        setStackFrom(StackFrom.values()[array.getInt(R.styleable.CardStackView_stackFrom, option.stackFrom.ordinal())]);
        setElevationEnabled(array.getBoolean(R.styleable.CardStackView_elevationEnabled, option.isElevationEnabled));
        setSwipeEnabled(array.getBoolean(R.styleable.CardStackView_swipeEnabled, option.isSwipeEnabled));
        setMultipleReverseEnabled(array.getBoolean(R.styleable.CardStackView_multipleReverseEnabled, option.isMultipleReverseEnabled));
        setSwipeDirection(SwipeDirection.from(array.getInt(R.styleable.CardStackView_swipeDirection, 0)));
        setReverseDirection(SwipeDirection.from(array.getInt(R.styleable.CardStackView_reverseDirection, 0)));
        setLeftOverlay(array.getResourceId(R.styleable.CardStackView_leftOverlay, 0));
        setRightOverlay(array.getResourceId(R.styleable.CardStackView_rightOverlay, 0));
        setBottomOverlay(array.getResourceId(R.styleable.CardStackView_bottomOverlay, 0));
        setTopOverlay(array.getResourceId(R.styleable.CardStackView_topOverlay, 0));
        array.recycle();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (state.isInitialized && visibility == View.VISIBLE) {
            initializeCardStackPosition();
        }
    }

    private void initialize(boolean shouldReset) {
        resetIfNeeded(shouldReset);
        initializeViews();
        initializeCardStackPosition();
        initializeViewContents();
    }

    private void resetIfNeeded(boolean shouldReset) {
        if (shouldReset) {
            state.reset();
        }
    }

    private void initializeViews() {
        removeAllViews();
        containers.clear();

        try {
            Constructor<?> viewConstructor = cardContainerViewClass.getConstructor(Context.class);

            for (int i = 0; i < option.visibleCount; i++) {
                try {
                    CardContainerView view = (CardContainerView) viewConstructor.newInstance(getContext());

                    view.setDraggable(false);
                    view.setCardStackOption(option);
                    view.setOverlay(option.leftOverlay, option.rightOverlay, option.bottomOverlay, option.topOverlay);
                    containers.add(0, view);
                    addView(view);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        getTopView().setContainerEventListener(containerEventListener);

        state.isInitialized = true;
    }

    private void initializeCardStackPosition() {
        clear();
        update(0f, 0f);
    }

    private void initializeViewContents() {
        for (int i = 0; i < option.visibleCount; i++) {
            CardContainerView container = containers.get(i);
            int adapterIndex = state.topIndex + i;

            if (adapterIndex < adapter.getCount()) {
                ViewGroup parent = container.getContentContainer();
                View child = adapter.getView(adapterIndex, parent.getChildAt(0), parent);
                if (parent.getChildCount() == 0) {
                    parent.addView(child);
                }
                container.setVisibility(View.VISIBLE);
            } else {
                container.setVisibility(View.GONE);
            }
        }
        if (!adapter.isEmpty()) {
            getTopView().setDraggable(true);
        }
    }

    private void loadNextView() {
        int lastIndex = findAvailableIndex(state.topIndex + option.visibleCount - 1, true, false);
        // increase index by unavailable items
        for (int i = state.topIndex + 1; i < state.topIndex + option.visibleCount; i++) {
            if (state.swipedItems.get(i) != null) {
                lastIndex++;
            }
        }

        boolean hasNextCard = lastIndex < adapter.getCount();
        if (hasNextCard) {
            CardContainerView container = getBottomView();
            container.setDraggable(false);
            ViewGroup parent = container.getContentContainer();
            View child = adapter.getView(lastIndex, parent.getChildAt(0), parent);
            if (parent.getChildCount() == 0) {
                parent.addView(child);
            }
        } else {
            CardContainerView container = getBottomView();
            container.setDraggable(false);
            container.setVisibility(View.GONE);
        }

        boolean hasCard = state.topIndex < adapter.getCount();
        if (hasCard) {
            getTopView().setDraggable(true);
        }
    }

    private void clear() {
        for (int i = 0; i < option.visibleCount; i++) {
            CardContainerView view = containers.get(i);
            view.reset();
            ViewCompat.setTranslationX(view, 0f);
            ViewCompat.setTranslationY(view, 0f);
            ViewCompat.setScaleX(view, 1f);
            ViewCompat.setScaleY(view, 1f);
            ViewCompat.setRotation(view, 0f);
        }
    }

    private void update(float percentX, float percentY) {
        if (cardEventListener != null) {
            cardEventListener.onCardDragging(percentX, percentY);
        }

        if (!option.isElevationEnabled) {
            return;
        }

        for (int i = 1; i < option.visibleCount; i++) {
            CardContainerView view = containers.get(i);

            float currentScale = 1f - (i * option.scaleDiff);
            float nextScale = 1f - ((i - 1) * option.scaleDiff);
            float percent = currentScale + (nextScale - currentScale) * Math.abs(percentX);
            ViewCompat.setScaleX(view, percent);
            ViewCompat.setScaleY(view, percent);

            float currentTranslationY = i * Util.toPx(getContext(), option.translationDiff);
            if (option.stackFrom == StackFrom.Top) {
                currentTranslationY *= -1;
            }

            float nextTranslationY = (i - 1) * Util.toPx(getContext(), option.translationDiff);
            if (option.stackFrom == StackFrom.Top) {
                nextTranslationY *= -1;
            }

            float translationY = currentTranslationY - Math.abs(percentX) * (currentTranslationY - nextTranslationY);
            ViewCompat.setTranslationY(view, translationY);
        }
    }

    public void performReverse(Point point, View prevView, final Animator.AnimatorListener listener) {
        // disable draggable the top card will be reordered to the second
        getTopView().setDraggable(false);

        reorderForReverse(prevView);
        CardContainerView topView = getTopView();
        ViewCompat.setTranslationX(topView, point.x);
        ViewCompat.setTranslationY(topView, -point.y);
        topView.animate()
                .translationX(topView.getViewOriginX())
                .translationY(topView.getViewOriginY())
                .setListener(listener)
                .setDuration(400L)
                .start();
    }

    public void performSwipe(Point point, final Animator.AnimatorListener listener) {
        getTopView().animate()
                .translationX(point.x)
                .translationY(-point.y)
                .setDuration(400L)
                .setListener(listener)
                .start();
    }

    public void performSwipe(SwipeDirection direction, AnimatorSet set, final Animator.AnimatorListener listener) {
        if (direction == SwipeDirection.LEFT) {
            getTopView().showLeftOverlay();
            getTopView().setOverlayAlpha(1f);
        } else if (direction == SwipeDirection.RIGHT) {
            getTopView().showRightOverlay();
            getTopView().setOverlayAlpha(1f);
        } else if (direction == SwipeDirection.BOTTOM) {
            getTopView().showBottomOverlay();
            getTopView().setOverlayAlpha(1f);
        } else if (direction == SwipeDirection.TOP) {
            getTopView().showTopOverlay();
            getTopView().setOverlayAlpha(1f);
        }
        set.addListener(listener);
        set.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                CardContainerView view = getTopView();
                update(view.getPercentX(), view.getPercentY());
                return input;
            }
        });
        set.start();
    }

    private void moveToBottom(CardContainerView container) {
        CardStackView parent = (CardStackView) container.getParent();
        if (parent != null) {
            parent.removeView(container);
            parent.addView(container, 0);
        }
    }

    private void moveToTop(CardContainerView container, View child) {
        CardStackView parent = (CardStackView) container.getParent();
        if (parent != null) {
            parent.removeView(container);
            parent.addView(container);

            container.getContentContainer().removeAllViews();
            container.getContentContainer().addView(child);
            container.setVisibility(View.VISIBLE);
        }
    }

    private void reorderForSwipe() {
        moveToBottom(getTopView());
        containers.addLast(containers.removeFirst());
    }

    private void reorderForReverse(View prevView) {
        CardContainerView bottomView = getBottomView();
        moveToTop(bottomView, prevView);
        containers.addFirst(containers.removeLast());
    }

    private void executePreSwipeTask() {
        getTopView().setContainerEventListener(null);
        getTopView().setDraggable(false);
        if (containers.size() > 1) {
            containers.get(1).setContainerEventListener(containerEventListener);
            containers.get(1).setDraggable(true);
        }
    }

    private void executePostSwipeTask(Point point, SwipeDirection direction) {
        reorderForSwipe();

        initializeCardStackPosition();

        if (!option.isMultipleReverseEnabled) {
            state.swipedItems.clear();
        }
        state.swipedItems.put(state.topIndex, new SwipedItem(point, direction));
        state.topIndex = findAvailableIndex(state.topIndex + 1, true);

        if (cardEventListener != null) {
            cardEventListener.onCardSwiped(direction);
        }

        loadNextView();

        getBottomView().setContainerEventListener(null);
        getTopView().setContainerEventListener(containerEventListener);
    }

    private void executePostReverseTask(int reverseIndex) {
        initializeCardStackPosition();

        state.topIndex = reverseIndex;
        state.swipedItems.remove(state.topIndex);

        if (cardEventListener != null) {
            cardEventListener.onCardReversed();
        }

        getBottomView().setContainerEventListener(null);
        getTopView().setContainerEventListener(containerEventListener);

        getTopView().setDraggable(true);
    }

    public void setCardEventListener(CardEventListener listener) {
        this.cardEventListener = listener;
    }

    public void setAdapter(BaseAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = adapter;
        this.adapter.registerDataSetObserver(dataSetObserver);
        this.state.lastCount = adapter.getCount();
        initialize(true);
    }

    public void setVisibleCount(int visibleCount) {
        option.visibleCount = visibleCount;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setSwipeThreshold(float swipeThreshold) {
        option.swipeThreshold = swipeThreshold;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setTranslationDiff(float translationDiff) {
        option.translationDiff = translationDiff;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setScaleDiff(float scaleDiff) {
        option.scaleDiff = scaleDiff;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setStackFrom(StackFrom stackFrom) {
        option.stackFrom = stackFrom;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setElevationEnabled(boolean isElevationEnabled) {
        option.isElevationEnabled = isElevationEnabled;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setSwipeEnabled(boolean isSwipeEnabled) {
        option.isSwipeEnabled = isSwipeEnabled;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setMultipleReverseEnabled(boolean isMultipleReverseEnabled) {
        option.isMultipleReverseEnabled = isMultipleReverseEnabled;
    }

    public void setSwipeDirection(@NonNull List<SwipeDirection> swipeDirection) {
        option.swipeDirection = swipeDirection;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setReverseDirection(@NonNull List<SwipeDirection> reverseDirection) {
        option.reverseDirection = reverseDirection;
    }

    public void setLeftOverlay(int leftOverlay) {
        option.leftOverlay = leftOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setRightOverlay(int rightOverlay) {
        option.rightOverlay = rightOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setBottomOverlay(int bottomOverlay) {
        option.bottomOverlay = bottomOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setTopOverlay(int topOverlay) {
        option.topOverlay = topOverlay;
        if (adapter != null) {
            initialize(false);
        }
    }

    public void setPaginationReserved() {
        state.isPaginationReserved = true;
    }

    public void setCardContainerViewClass(Class<?> cardContainerViewClass) {
        this.cardContainerViewClass = cardContainerViewClass;
        if (adapter != null) {
            initialize(true);
        }
    }

    public void swipe(final Point point, final SwipeDirection direction) {
        executePreSwipeTask();
        performSwipe(point, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                executePostSwipeTask(point, direction);
            }
        });
    }

    public void swipe(final SwipeDirection direction, AnimatorSet set) {
        executePreSwipeTask();
        performSwipe(direction, set, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                executePostSwipeTask(new Point(0, -2000), direction);
            }
        });
    }

    public void reverse() {
        reverse(true);
    }

    public void reverse(boolean directionLimit) {
        final int reverseIndex = findAvailableIndex(state.topIndex - 1, false, directionLimit);
        if (!state.isReversing && reverseIndex >= 0) {
            state.isReversing = true;
            CardContainerView container = getBottomView();
            ViewGroup parent = container.getContentContainer();
            View prevView = adapter.getView(reverseIndex, null, parent);
            Point point = state.swipedItems.get(reverseIndex).getPoint();

            performReverse(point, prevView, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    executePostReverseTask(reverseIndex);
                    state.isReversing = false;
                }
            });
        }
    }

    public CardContainerView getTopView() {
        return containers.getFirst();
    }

    public CardContainerView getBottomView() {
        return containers.getLast();
    }

    public int getTopIndex() {
        return state.topIndex;
    }

    public boolean isReversible() {
        return isReversible(true);
    }

    public boolean isReversible(boolean directionLimit) {
        return findAvailableIndex(state.topIndex - 1, false, directionLimit) >= 0;
    }

    private int findAvailableIndex(int index, boolean moveForward) {
        return findAvailableIndex(index, moveForward, true);
    }

    private int findAvailableIndex(int index, boolean moveForward, boolean directionLimit) {
        while (state.swipedItems.get(index) != null) {
            if (directionLimit && !option.reverseDirection.contains(state.swipedItems.get(index).getDirection())) {
                index = index + (moveForward ? 1 : -1);
            } else {
                break;
            }
        }
        return index;
    }
}
