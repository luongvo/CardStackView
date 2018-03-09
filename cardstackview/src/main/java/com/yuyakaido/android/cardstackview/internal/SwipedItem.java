package com.yuyakaido.android.cardstackview.internal;

import android.graphics.Point;

import com.yuyakaido.android.cardstackview.SwipeDirection;

/**
 * Created by luongvo on 3/5/18.
 */

public class SwipedItem {

    private Point point;
    private SwipeDirection direction;

    public SwipedItem(Point point, SwipeDirection direction) {
        this.point = point;
        this.direction = direction;
    }

    public Point getPoint() {
        return point;
    }

    public SwipeDirection getDirection() {
        return direction;
    }
}
