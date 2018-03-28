package com.yuyakaido.android.cardstackview.internal;

import android.util.SparseArray;

public class CardStackState {
    public int topIndex = 0;
    public boolean isPaginationReserved = false;
    public boolean isInitialized = false;
    public boolean isReversing = false;
    public SparseArray<SwipedItem> swipedItems = new SparseArray<>();

    public void reset() {
        topIndex = 0;
        isPaginationReserved = false;
        isInitialized = false;
        isReversing = false;
        swipedItems.clear();
    }
}
