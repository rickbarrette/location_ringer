/**
 * onContentChangedListener.java
 * @date Aug 7, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger;

import android.content.ContentValues;


/**
 * This interface will be used to pass the content updated in fragments down to the main FragmentActivity
 * @author ricky
 */
public interface OnContentChangedListener {

	void onRingerContentChanged(ContentValues ringer);
	
	void onInfoContentChanged(ContentValues info);
	
}