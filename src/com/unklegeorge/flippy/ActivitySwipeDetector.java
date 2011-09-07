package com.unklegeorge.flippy;

import android.view.MotionEvent;
import android.view.View;

abstract public class ActivitySwipeDetector implements View.OnTouchListener {

	static final String logTag = "ActivitySwipeDetector";
	static final int MIN_DISTANCE = 100;
	private float downX, downY, upX, upY;
	enum Type {
		LeftToRight,
		RightToLeft,
		TopToBottom,
		BottomToTop
	};

	abstract public boolean onSwipe(Type type, View view);
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
	    switch(event.getAction()){
	        case MotionEvent.ACTION_DOWN: {
	            downX = event.getX();
	            downY = event.getY();
	            return true;
	        }
	        case MotionEvent.ACTION_UP: {
	            upX = event.getX();
	            upY = event.getY();

	            float deltaX = downX - upX;
	            float deltaY = downY - upY;

	            // swipe horizontal?
	            if(Math.abs(deltaX) > MIN_DISTANCE){
	                // left or right
	                if(deltaX < 0) { return onSwipe(Type.LeftToRight, v); }
	                if(deltaX > 0) { return onSwipe(Type.RightToLeft, v); }
	            }

	            // swipe vertical?
	            if(Math.abs(deltaY) > MIN_DISTANCE){
	                // top or down
	                if(deltaY < 0) { return onSwipe(Type.TopToBottom, v); }
	                if(deltaY > 0) { return onSwipe(Type.BottomToTop, v); }
	            }
	            return true;
	        }
	    }
	    return false;
	}
	
}
