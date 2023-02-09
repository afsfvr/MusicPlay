package com.example.musicplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.viewpager.widget.ViewPager;

public class MyViewPager extends ViewPager {

    boolean isSliding = false;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (! isSliding) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (! isSliding) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    public void setSliding(boolean sliding) {
        isSliding = sliding;
    }
}
