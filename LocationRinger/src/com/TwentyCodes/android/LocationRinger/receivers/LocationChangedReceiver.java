/**
 * LocationReceiver.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.receivers;

import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.services.RingerProcessingService;
import com.TwentyCodes.android.debug.LocationLibraryConstants;
import com.TwentyCodes.android.location.BaseLocationReceiver;

/**
 * This class will receive broadcast from the location service. it will wake the
 * ringer processing service.
 * 
 * @author ricky barrette
 */
public class LocationChangedReceiver extends BaseLocationReceiver {

	protected static String TAG = "LocationReceiver";

	@Override
	public void onLocationUpdate(final Location location) {
		if (location != null)
			if (location.getAccuracy() <= Debug.IGNORE)
				mContext.startService(new Intent(mContext, RingerProcessingService.class).putExtra(LocationLibraryConstants.INTENT_EXTRA_LOCATION_CHANGED, location));
			else if (Debug.DEBUG)
				Log.d(TAG, "location accuracy = " + location.getAccuracy() + " ignoring");
			else if (Debug.DEBUG)
				Log.d(TAG, "location was null");
	}
}