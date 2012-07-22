/**
 * LocationService.java
 * @date Jul 3, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import anroid.v4.compat.NotificationCompat;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.ui.ListActivity;
import com.TwentyCodes.android.LocationRinger.ui.SettingsActivity;
import com.TwentyCodes.android.debug.LocationLibraryConstants;
import com.TwentyCodes.android.exception.ExceptionHandler;

/**
 * We override the location service so we can attach the exception handler
 * @author ricky barrette
 */
public class LocationService extends com.TwentyCodes.android.location.LocationService {

	private final int GATHERING_LOCATION_ONGING_NOTIFICATION_ID = 232903877;
	private SharedPreferences mSettings;
	private NotificationManager mNotificationManager;

	/**
	 * convince method for getting the single shot service intent
	 * @param context
	 * @return service intent
	 * @author ricky barrette
	 */
	public static Intent getSingleShotServiceIntent(Context context) {
		return new Intent(context, LocationService.class)
			.putExtra(LocationLibraryConstants.INTENT_EXTRA_REQUIRED_ACCURACY, Debug.ACCURACY)
			.setAction(LocationLibraryConstants.INTENT_ACTION_UPDATE);
	}
	
	/* (non-Javadoc)
	 * @see com.TwentyCodes.android.SkyHook.SkyHookService#onCreate()
	 * @author ricky barrette
	 */
	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		this.mSettings = this.getSharedPreferences(SettingsActivity.SETTINGS, Debug.SHARED_PREFS_MODE);
		this.mSettings.edit().putBoolean(SettingsActivity.IS_SERVICE_STARTED, true).commit();
		startOnGoingNotification();
		super.onCreate();
	}
	
	/* (non-Javadoc)
	 * @see com.TwentyCodes.android.SkyHook.SkyHookService#onDestroy()
	 * @author ricky barrette
	 */
	@Override
	public void onDestroy() {
		this.mSettings.edit().remove(SettingsActivity.IS_SERVICE_STARTED).commit();
		this.mNotificationManager.cancel(this.GATHERING_LOCATION_ONGING_NOTIFICATION_ID);
		super.onDestroy();
	}
	
	/* (non-Javadoc)
	 * @see com.TwentyCodes.android.SkyHook.SkyHookService#onStartCommand(android.content.Intent, int, int)
	 * @author ricky barrette
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.mPeriod = Debug.UPDATE_INTERVAL;
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Starts the location service in multi shot mode
	 * @param context
	 * @return
	 * @author ricky barrette
	 */
	public static ComponentName startMultiShotService(final Context context){
		Intent i = getSingleShotServiceIntent(context).putExtra(LocationLibraryConstants.INTENT_EXTRA_PERIOD_BETWEEN_UPDATES, Debug.UPDATE_INTERVAL);
		return context.startService(i);
	}
	
	/**
	 * starts a simple ongoing notification to inform the user that we are gathering location
	 * @author ricky barrette
	 */
	private void startOnGoingNotification() {
		this.mNotificationManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
			.setContentTitle(getString(R.string.app_name))
			.setContentText(this.getString(R.string.gathering))
			.setTicker(this.getString(R.string.gathering))
			.setSmallIcon(R.drawable.icon)
			.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, ListActivity.class), android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
			.setWhen(System.currentTimeMillis())
			.setOngoing(true);
		
		this.mNotificationManager.notify(this.GATHERING_LOCATION_ONGING_NOTIFICATION_ID, builder.getNotification());
	}

	/**
	 * starts the service in single shot mode
	 * @param context
	 * @return
	 * @author ricky barrette
	 */
	public static ComponentName startSingleShotService(final Context context){
		return context.startService(getSingleShotServiceIntent(context));
	}
}