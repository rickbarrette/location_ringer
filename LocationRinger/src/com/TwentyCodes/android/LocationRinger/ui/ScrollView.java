/**
 * ScrollView.java
 * @date May 3, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * This is a simple scroll view that i have made to enable and disable scrolling
 * @author ricky barrette
 */
public class ScrollView extends android.widget.ScrollView {

	private boolean isEnabled = true;

	/**
	 * @param context
	 * @param apiKey
	 * @author ricky barrette
	 */
	public ScrollView(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @author ricky barrette
	 */
	public ScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 * @author ricky barrette
	 */
	public ScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * Enables or disabled the scrollview's ability to scroll
	 * @param enabled
	 * @author ricky barrette
	 */
	public void setScrollEnabled(boolean enabled){
		isEnabled = enabled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return isEnabled ? super.onInterceptTouchEvent(ev) : false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return isEnabled ? super.onTouchEvent(ev) : false;
	}

}
