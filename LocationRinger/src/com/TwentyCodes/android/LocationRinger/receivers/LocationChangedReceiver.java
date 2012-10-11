/**
 * LocationReceiver.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.receivers;

import android.content.Intent;
import android.location.Location;

import com.TwentyCodes.android.LocationRinger.Constraints;
import com.TwentyCodes.android.LocationRinger.Log;
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
			if (location.getAccuracy() <= Constraints.IGNORE)
				mContext.startService(new Intent(mContext, RingerProcessingService.class).putExtra(LocationLibraryConstants.INTENT_EXTRA_LOCATION_CHANGED, location));
		Log.d(TAG, "location accuracy = " + location.getAccuracy() + " ignoring");
		Log.d(TAG, "location was null");
	}
}