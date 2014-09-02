/**
 * LocationReceiver.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.receivers;

import org.RickBarrette.android.LocationRinger.Constraints;
import org.RickBarrette.android.LocationRinger.Log;
import org.RickBarrette.android.LocationRinger.services.RingerProcessingService;

import android.content.Intent;
import android.location.Location;

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
		if (location != null) {
			if (location.getAccuracy() <= Constraints.IGNORE)
				mContext.startService(new Intent(mContext, RingerProcessingService.class).putExtra(LocationLibraryConstants.INTENT_EXTRA_LOCATION_CHANGED, location));
			else if (Constraints.VERBOSE)
				Log.v(TAG, "location accuracy = " + location.getAccuracy() + " ignoring");
		} else if(Constraints.VERBOSE)
				Log.v(TAG, "location was null");
	}
}