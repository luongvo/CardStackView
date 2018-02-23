package com.yuyakaido.android.cardstackview.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Copyright © 2017 buddify.io
 * <p/>
 * Created by luongvo on 2/22/18.
 */

public class CustomButton extends Button {

    private static final int MIN_DISTANCE_X = 50;
    private static final int MIN_DISTANCE_Y = 50;
    private float downX, downY;

    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                downX = event.getX();
//                downY = event.getY();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//
//                float upX = event.getX();
//                float upY = event.getY();
//                float deltaX = downX - upX;
//                float deltaY = downY - upY;
//
//                if (Math.abs(deltaX) > MIN_DISTANCE_X || Math.abs(deltaY) > MIN_DISTANCE_Y) {
//                    return false;
//                }
//        }
//        return super.dispatchTouchEvent(event);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (e.getX() < 0 || e.getY() < 0 || e.getX() > getMeasuredWidth() || e.getY() > getMeasuredHeight()) {
//            Log.i("TAG", "TOUCH OUTSIDE");
////            e.setAction(MotionEvent.ACTION_CANCEL);
//            return false;
//        }
//////        else
//////            Log.i("TAG", "TOUCH INSIDE");
//////
//////        switch (e.getAction()) {
//////            case MotionEvent.ACTION_DOWN:
//////                Log.w("TAG", "ACTION_DOWN");
//////                break;
//////            case MotionEvent.ACTION_UP:
//////                Log.w("TAG", "ACTION_UP");
//////                break;
//////            case MotionEvent.ACTION_CANCEL:
//////                Log.w("TAG", "ACTION_CANCEL");
//////                break;
//////            case MotionEvent.ACTION_MOVE:
//////                Log.w("TAG", "ACTION_MOVE");
//////                break;
//////        }
//        Log.w("TAG", e.getAction() + "_");


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:

                float upX = event.getX();
                float upY = event.getY();
                float deltaX = downX - upX;
                float deltaY = downY - upY;

                if (Math.abs(deltaX) > MIN_DISTANCE_X || Math.abs(deltaY) > MIN_DISTANCE_Y) {
                    Log.e("====", "cancel");
                    setPressed(false);
                }
        }
        return super.onTouchEvent(event);
////        return false;
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                downX = event.getX();
//                downY = event.getY();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//
//                float upX = event.getX();
//                float upY = event.getY();
//                float deltaX = downX - upX;
//                float deltaY = downY - upY;
//
//                if (Math.abs(deltaX) > MIN_DISTANCE_X || Math.abs(deltaY) > MIN_DISTANCE_Y) {
//
//                }
//        }
//        super.onTouchEvent(event);
//    }
}
