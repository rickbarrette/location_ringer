/**
 * FeatureRemovedListener.java
 * @date May 26, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package org.RickBarrette.android.LocationRinger;

import android.support.v4.app.Fragment;

/**
 * This interface will be used to notify
 * 
 * @author ricky barrette
 */
public interface FeatureRemovedListener {

	/**
	 * Called when a feature is removed from the list
	 * 
	 * @author ricky barrette
	 */
	public void onFeatureRemoved(Fragment fragment);
}