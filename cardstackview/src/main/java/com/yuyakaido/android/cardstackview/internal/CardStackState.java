package com.yuyakaido.android.cardstackview.internal;

import android.graphics.Point;
import android.util.SparseArray;

import java.util.HashSet;
import java.util.Set;

public class CardStackState {
    public int topIndex = 0;
    public int lastCount = 0;
    public boolean isPaginationReserved = false;
    public boolean isInitialized = false;
    public boolean isReversing = false;
    public SparseArray<Point> swipedItems = new SparseArray<>();

    public void reset() {
        topIndex = 0;
        lastCount = 0;
        isPaginationReserved = false;
        isInitialized = false;
        swipedItems.clear();
    }
}
