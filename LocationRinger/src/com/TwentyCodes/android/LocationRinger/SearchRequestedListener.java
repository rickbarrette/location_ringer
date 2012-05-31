/**
 * SearchRequestedListener.java
 * @date May 31, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.LocationRinger;

/**
 * A simple interface to allow a compent other than an activity to handle a seach event
 * @author ricky barrette
 */
public interface SearchRequestedListener {
	
	/**
	 * Called when the a seach is request via seach button
	 * @return
	 * @author ricky barrette
	 */
	public boolean onSearchRequested();
}