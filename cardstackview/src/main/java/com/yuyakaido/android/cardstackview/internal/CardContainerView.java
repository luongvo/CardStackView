package com.yuyakaido.android.cardstackview.internal;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.yuyakaido.android.cardstackview.R;
import com.yuyakaido.android.cardstackview.SwipeDirection;

public class CardContainerView extends FrameLayout {

    private static String TAG = CardContainerView.class.getSimpleName();
    private CardStackOption option;

    private float viewOriginX = 0f;
    private float viewOriginY = 0f;
    private float motionOriginX = 0f;
    private float motionOriginY = 0f;
    private boolean isDragging = false;
    private boolean isDraggable = true;

    private ViewGroup contentContainer = null;
    private ViewGroup overlayContainer = null;
    private View leftOverlayView = null;
    private View rightOverlayView = null;
    private View bottomOverlayView = null;
    private View topOverlayView = null;

    private ContainerEventListener containerEventListener = null;
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (containerEventListener != null) {
                containerEventListener.onContainerClicked();
            }
            return true;
        }
    };
    private GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);

    public interface ContainerEventListener {
        void onContainerDragging(float percentX, float percentY);

        void onContainerSwiped(Point point, SwipeDirection direction);

        void onContainerMovedToOrigin();

        void onContainerClicked();
    }

    public CardContainerView(Context context) {
        this(context, null);
    }

    public CardContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflate(getContext(), R.layout.card_frame, this);
        contentContainer = (ViewGroup) findViewById(R.id.card_frame_content_container);
        overlayContainer = (ViewGroup) findViewById(R.id.card_frame_overlay_container);
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        super.dispatchTouchEvent(event);
//        return true;
//
//                switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                initY = (int) event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (Math.abs((int) event.getY() - initY) > THRESHOLD) {
//                    return true;
//                }
//                break;
////                if (((int) event.getY() - initY) > THRESHOLD) {
////                    // move down
//////                    return event.getY() >= appBarOffset && super.onInterceptTouchEvent(event);
////                    return true;
////                }
////
////                if (((int) event.getY() - initY) > -THRESHOLD) {
////                    // move up
//////                    return super.onInterceptTouchEvent(event);
////                    return true;
////                }
////                break;
////            case MotionEvent.ACTION_UP:
////                if (Math.abs((int) event.getY() - initY) < THRESHOLD) {
////                    return false;
////                } else {
////                    return true;
////                }
////                break;
//        }
//
//
//
//
//        // do what you need to with the event, and then...
//        return super.dispatchTouchEvent(event);
//        return true;
////        return false;
//
//    }
//
//    private static int THRESHOLD = 20;
//    private double initY;
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
////        return super.onInterceptTouchEvent(ev);
////        return true;
////        return false;
//
////        switch (event.getAction()) {
////            case MotionEvent.ACTION_DOWN:
////                initY = (int) event.getY();
////                break;
////            case MotionEvent.ACTION_MOVE:
////                if (Math.abs((int) event.getY() - initY) < THRESHOLD) {
////                    return false;
////                }
////                break;
//////                if (((int) event.getY() - initY) > THRESHOLD) {
//////                    // move down
////////                    return event.getY() >= appBarOffset && super.onInterceptTouchEvent(event);
//////                    return true;
//////                }
//////
//////                if (((int) event.getY() - initY) > -THRESHOLD) {
//////                    // move up
////////                    return super.onInterceptTouchEvent(event);
//////                    return true;
//////                }
//////                break;
//////            case MotionEvent.ACTION_UP:
//////                if (Math.abs((int) event.getY() - initY) < THRESHOLD) {
//////                    return false;
//////                } else {
//////                    return true;
//////                }
//////                break;
////        }
//        return super.onInterceptTouchEvent(event);
//    }

    private static final int MIN_DISTANCE_X = 100;
    private static final int MIN_DISTANCE_Y = 100;
    private float downX, downY;

    /**
     * Spy or monitor all the events include those been sent to child views
     * ref: https://stackoverflow.com/a/35113182/2100084
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // handle the motion event even if a child returns true in OnTouchEvent
        // the MotionEvent may have been canceled by the child view
        handleTouchEvent(event);

        boolean send = true;
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                downX = event.getX();
//                downY = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float upX = event.getX();
//                float upY = event.getY();
//                float deltaX = downX - upX;
//                float deltaY = downY - upY;
//
//                if (Math.abs(deltaX) > MIN_DISTANCE_X || Math.abs(deltaY) > MIN_DISTANCE_Y) {
//                    send = false;
//                }
//                break;
//        }

        if (send) {
            super.dispatchTouchEvent(event);
        }

        // to keep receive event that follow down event
        return true;
    }

//    private static final int MIN_DISTANCE_X = 25;
//    private static final int MIN_DISTANCE_Y = 5;
//    private float downX, downY;
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        Log.d(TAG, "onInterceptTouchEvent : " + event.getAction());
//
////        switch (event.getAction()) {
////
////            case MotionEvent.ACTION_DOWN:
////                downX = event.getX();
////                downY = event.getY();
////                break;
////
////            case MotionEvent.ACTION_UP:
////
////                float upX = event.getX();
////                float upY = event.getY();
////                float deltaX = downX - upX;
////                float deltaY = downY - upY;
////
////                if (Math.abs(deltaX) < MIN_DISTANCE_X && Math.abs(deltaY) < MIN_DISTANCE_Y) {
////                    return false;
////                } else {
////                    // todo
////                }
////        }
//
////        super.onTouchEvent(event);
//        return super.onInterceptTouchEvent(event);
//
////        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // OnTouchEvent is called manually, if OnTouchEvent propagates back to this layout do
        // nothing as it was already handled.
        return true;
    }

    private boolean handleTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if (!option.isSwipeEnabled || !isDraggable) {
            return true;
        }

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                getParent().getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouchEvent" + event.getAction());
                handleActionUp(event);
                getParent().getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
        }

        return true;
    }

    private void handleActionDown(MotionEvent event) {
        motionOriginX = event.getRawX();
        motionOriginY = event.getRawY();
    }

    private void handleActionUp(MotionEvent event) {
        if (isDragging) {
            isDragging = false;

            float motionCurrentX = event.getRawX();
            float motionCurrentY = event.getRawY();

            Point point = Util.getTargetPoint(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY);
            Quadrant quadrant = Util.getQuadrant(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY);
            double radian = Util.getRadian(motionOriginX, motionOriginY, motionCurrentX, motionCurrentY);
            double degree = 0;
            SwipeDirection direction = null;
            switch (quadrant) {
                case TopLeft:
                    degree = Math.toDegrees(radian);
                    degree = 180 - degree;
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < -0.5) {
                        direction = SwipeDirection.Left;
                    } else {
                        direction = SwipeDirection.Top;
                    }
                    break;
                case TopRight:
                    degree = Math.toDegrees(radian);
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < 0.5) {
                        direction = SwipeDirection.Top;
                    } else {
                        direction = SwipeDirection.Right;
                    }
                    break;
                case BottomLeft:
                    degree = Math.toDegrees(radian);
                    degree = 180 + degree;
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < -0.5) {
                        direction = SwipeDirection.Left;
                    } else {
                        direction = SwipeDirection.Bottom;
                    }
                    break;
                case BottomRight:
                    degree = Math.toDegrees(radian);
                    degree = 360 - degree;
                    radian = Math.toRadians(degree);
                    if (Math.cos(radian) < 0.5) {
                        direction = SwipeDirection.Bottom;
                    } else {
                        direction = SwipeDirection.Right;
                    }
                    break;
            }

            float percent = 0f;
            if (direction == SwipeDirection.Left || direction == SwipeDirection.Right) {
                percent = getPercentX();
            } else {
                percent = getPercentY();
            }

            if (Math.abs(percent) > option.swipeThreshold) {
                if (option.swipeDirection.contains(direction)) {
                    if (containerEventListener != null) {
                        containerEventListener.onContainerSwiped(point, direction);
                    }
                } else {
                    moveToOrigin();
                    if (containerEventListener != null) {
                        containerEventListener.onContainerMovedToOrigin();
                    }
                }
            } else {
                moveToOrigin();
                if (containerEventListener != null) {
                    containerEventListener.onContainerMovedToOrigin();
                }
            }
        }

        motionOriginX = event.getRawX();
        motionOriginY = event.getRawY();
    }

    private void handleActionMove(MotionEvent event) {
        isDragging = true;

        updateTranslation(event);
        updateRotation();
        updateAlpha();

        if (containerEventListener != null) {
            containerEventListener.onContainerDragging(getPercentX(), getPercentY());
        }
    }

    private void updateTranslation(MotionEvent event) {
        ViewCompat.setTranslationX(this, viewOriginX + event.getRawX() - motionOriginX);
        ViewCompat.setTranslationY(this, viewOriginY + event.getRawY() - motionOriginY);
    }

    private void updateRotation() {
        ViewCompat.setRotation(this, getPercentX() * 20);
    }

    private void updateAlpha() {
        float percentX = getPercentX();
        float percentY = getPercentY();

        if (Math.abs(percentX) > Math.abs(percentY)) {
            if (percentX < 0) {
                showLeftOverlay();
            } else {
                showRightOverlay();
            }
            setOverlayAlpha(Math.abs(percentX));
        } else {
            if (percentY < 0) {
                showTopOverlay();
            } else {
                showBottomOverlay();
            }
            setOverlayAlpha(Math.abs(percentY));
        }
    }

    private void moveToOrigin() {
        animate().translationX(viewOriginX)
                .translationY(viewOriginY)
                .setDuration(300L)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .setListener(null)
                .start();
    }

    public void setContainerEventListener(ContainerEventListener listener) {
        this.containerEventListener = listener;
        viewOriginX = ViewCompat.getTranslationX(this);
        viewOriginY = ViewCompat.getTranslationY(this);
    }

    public void setCardStackOption(CardStackOption option) {
        this.option = option;
    }

    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    public void reset() {
        ViewCompat.setAlpha(contentContainer, 1f);
        ViewCompat.setAlpha(overlayContainer, 0f);
    }

    public ViewGroup getContentContainer() {
        return contentContainer;
    }

    public ViewGroup getOverlayContainer() {
        return overlayContainer;
    }

    public void setOverlay(int left, int right, int bottom, int top) {
        if (leftOverlayView != null) {
            overlayContainer.removeView(leftOverlayView);
        }
        if (left != 0) {
            leftOverlayView = LayoutInflater.from(getContext()).inflate(left, overlayContainer, false);
            overlayContainer.addView(leftOverlayView);
            ViewCompat.setAlpha(leftOverlayView, 0f);
        }

        if (rightOverlayView != null) {
            overlayContainer.removeView(rightOverlayView);
        }
        if (right != 0) {
            rightOverlayView = LayoutInflater.from(getContext()).inflate(right, overlayContainer, false);
            overlayContainer.addView(rightOverlayView);
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            overlayContainer.removeView(bottomOverlayView);
        }
        if (bottom != 0) {
            bottomOverlayView = LayoutInflater.from(getContext()).inflate(bottom, overlayContainer, false);
            overlayContainer.addView(bottomOverlayView);
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }

        if (topOverlayView != null) {
            overlayContainer.removeView(topOverlayView);
        }
        if (top != 0) {
            topOverlayView = LayoutInflater.from(getContext()).inflate(top, overlayContainer, false);
            overlayContainer.addView(topOverlayView);
            ViewCompat.setAlpha(topOverlayView, 0f);
        }
    }

    public void setOverlayAlpha(float alpha) {
        ViewCompat.setAlpha(overlayContainer, alpha);
    }

    public void showLeftOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 1f);
        }
        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }
        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }
        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f);
        }
    }

    public void showRightOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }

        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f);
        }

        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 1f);
        }
    }

    public void showBottomOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 1f);
        }

        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 0f);
        }

        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }
    }


    public void showTopOverlay() {
        if (leftOverlayView != null) {
            ViewCompat.setAlpha(leftOverlayView, 0f);
        }

        if (bottomOverlayView != null) {
            ViewCompat.setAlpha(bottomOverlayView, 0f);
        }

        if (topOverlayView != null) {
            ViewCompat.setAlpha(topOverlayView, 1f);
        }

        if (rightOverlayView != null) {
            ViewCompat.setAlpha(rightOverlayView, 0f);
        }
    }

    public float getViewOriginX() {
        return viewOriginX;
    }

    public float getViewOriginY() {
        return viewOriginY;
    }

    public float getPercentX() {
        float percent = 2f * (ViewCompat.getTranslationX(this) - viewOriginX) / getWidth();
        if (percent > 1) {
            percent = 1;
        }
        if (percent < -1) {
            percent = -1;
        }
        return percent;
    }

    public float getPercentY() {
        float percent = 2f * (ViewCompat.getTranslationY(this) - viewOriginY) / getHeight();
        if (percent > 1) {
            percent = 1;
        }
        if (percent < -1) {
            percent = -1;
        }
        return percent;
    }

}
