/**
 * PassiveLocationChangedReceiver.java
 * @date May 15, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.LocationRinger.receivers;

import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.services.RingerProcessingService;
import com.TwentyCodes.android.LocationRinger.ui.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

/**
 * This class will be used to listen for location updates passively 
 * @author ricky barrette
 */
public class PassiveLocationChangedReceiver extends com.TwentyCodes.android.location.PassiveLocationChangedReceiver {

	private static final String TAG = "PassiveLocationChangedReceiver";
	
	/**
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.PassiveLocationChangedReceiver#onLocationUpdate(android.location.Location)
	 */
	@Override
	public void onLocationUpdate(Location location) {
		if(location != null)
			if(location.getAccuracy()<= Integer.parseInt(mContext.getSharedPreferences(SettingsActivity.SETTINGS, Context.MODE_PRIVATE).getString(SettingsActivity.IGNORE_LOCATION, "1000")))	
				mContext.startService(new Intent(mContext, RingerProcessingService.class).putExtra(LocationReceiver.INTENT_EXTRA_LOCATION_PARCEL, location));
			else
				if(Debug.DEBUG)
					Log.d(TAG, "location accuracy = "+ location.getAccuracy()+" ignoring");
		 else 
			if(Debug.DEBUG)
				Log.d(TAG, "location was null");
	}
}