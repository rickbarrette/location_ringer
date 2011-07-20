/**
 * LocatoinSelectedListener.java
 * @date May 9, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger;

import com.google.android.maps.GeoPoint;

/**
 * This interface will be used to pass the selected location from the dialogs to the listening instance
 * @author ricky barrette
 */
public interface LocationSelectedListener {

	public void onLocationSelected(GeoPoint point);
}