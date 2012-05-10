/**
 * ViewPager.java
 * @date Aug 13, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author ricky
 */
public class ViewPager extends android.support.v4.view.ViewPager {
	
	private boolean isEnabled = true;

	/**
	 * @param context
	 * @param apiKey
	 * @author ricky barrette
	 */
	public ViewPager(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @author ricky barrette
	 */
	public ViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
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