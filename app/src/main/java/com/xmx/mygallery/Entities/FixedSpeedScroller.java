package com.xmx.mygallery.Entities;

import android.content.Context;
import android.widget.Scroller;

/**
 * Created by The_onE on 2015/12/16.
 */
public class FixedSpeedScroller extends Scroller {
    private int mDuration = 0;

    public FixedSpeedScroller(Context context) {
        super(context);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}