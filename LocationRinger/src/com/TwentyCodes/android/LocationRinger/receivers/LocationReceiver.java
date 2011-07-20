/**
 * LocationReceiver.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.receivers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.services.RingerProcessingService;
import com.TwentyCodes.android.LocationRinger.ui.SettingsActivity;

/**
 * This class will receive broadcast from the location service. it will wake the ringer processing service.
 * @author ricky barrette
 */
public class LocationReceiver extends com.TwentyCodes.android.location.LocationReceiver {

	public static final String LR_ACTION_UPDATE = "com.TwentyCodes.android.LocationRinger.action.LocationUpdate";	
	private static final String TAG = "LocationReceiver";

	@Override
	public void onLocationUpdate(Location location) {
		if(location != null)
			if(location.getAccuracy()<= Integer.parseInt(mContext.getSharedPreferences(SettingsActivity.SETTINGS, Context.MODE_PRIVATE).getString(SettingsActivity.IGNORE_LOCATION, "1000")))	
				mContext.startService(new Intent(mContext, RingerProcessingService.class).putExtra(INTENT_EXTRA_LOCATION_PARCEL, location));
			else
				if(Debug.DEBUG)
					Log.d(TAG, "location accuracy = "+ location.getAccuracy()+" ignoring");
		 else 
			if(Debug.DEBUG)
				Log.d(TAG, "location was null");
	}
}