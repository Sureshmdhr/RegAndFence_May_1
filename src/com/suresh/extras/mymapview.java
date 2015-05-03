package com.suresh.extras;


import org.mapsforge.map.android.view.MapView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class mymapview extends MapView 
{
    public mymapview(Context context, AttributeSet attrs) 
    {
		super(context, attrs);
	}

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            // Disallow ScrollView to intercept touch events.
            this.getParent().requestDisallowInterceptTouchEvent(true);
            break;

        case MotionEvent.ACTION_UP:
            // Allow ScrollView to intercept touch events.
            this.getParent().requestDisallowInterceptTouchEvent(false);
            break;
        }

        // Handle MapView's touch events.
        super.onTouchEvent(ev);
        return true;
    }
}