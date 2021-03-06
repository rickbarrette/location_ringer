/**
 * SystemReceiver.java
 * @date May 4, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.receivers;

import org.RickBarrette.android.LocationRinger.Constraints;
import org.RickBarrette.android.LocationRinger.Log;
import org.RickBarrette.android.LocationRinger.services.LocationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.TwentyCodes.android.location.PassiveLocationListener;

/**
 * This receiver will system events
 * 
 * @author ricky barrette
 */
public class SystemReceiver extends BroadcastReceiver {

	/*
	 * these constants are used for checking the shared_prefs
	 */
	private final String BATTERY_LOW = "battery_low";
	private final String TAG = "SystemEventReciever";

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 *      android.content.Intent)
	 * @param context
	 * @param intent
	 * @author ricky barrette
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.d(TAG, "onReceive() ~" + intent.getAction());
		final SharedPreferences systemEventHistory = context.getSharedPreferences(TAG, Constraints.SHARED_PREFS_MODE);

		/*
		 * if the phone finishes booting, then start the service if the user
		 * enabled it
		 */
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
			// if (context.getSharedPreferences(SettingsActivity.SETTINGS,
			// Constraints.SHARED_PREFS_MODE).getBoolean(SettingsActivity.START_ON_BOOT,
			// false)) {
			LocationService.startMultiShotService(context);
		PassiveLocationListener.requestPassiveLocationUpdates(context, new Intent(context, PassiveLocationChangedReceiver.class));
		// }

		/*
		 * if the battery is reported to be low then stop the service, and
		 * remove the pending alarm and finally record that the phone's battery
		 * was low in the shared_prefs
		 */
		if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
			com.TwentyCodes.android.location.LocationService.stopService(context).run();
			new Handler().postDelayed(com.TwentyCodes.android.location.LocationService.stopService(context), 30000L);
			systemEventHistory.edit().putBoolean(BATTERY_LOW, true).commit();
		}

		/*
		 * if the phone is plugged in then check to see if the battery was
		 * reported low, if it was then restart the service, and remove
		 * shared_prefs entry
		 */
		if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
			if (systemEventHistory.getBoolean(BATTERY_LOW, false)) {
				systemEventHistory.edit().remove(BATTERY_LOW).commit();
				LocationService.startMultiShotService(context);
			}
	}

}
