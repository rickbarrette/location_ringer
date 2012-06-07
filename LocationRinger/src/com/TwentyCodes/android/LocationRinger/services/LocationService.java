/**
 * LocationService.java
 * @date Jul 3, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.ui.ListActivity;
import com.TwentyCodes.android.LocationRinger.ui.SettingsActivity;
import com.TwentyCodes.android.SkyHook.SkyHookService;
import com.TwentyCodes.android.exception.ExceptionHandler;

/**
 * We override the location service so we can attach the exception handler
 * @author ricky barrette
 */
public class LocationService extends SkyHookService {

	private final int GATHERING_LOCATION_ONGING_NOTIFICATION_ID = 232903877;
	private SharedPreferences mSettings;
	private NotificationManager mNotificationManager;

	/* (non-Javadoc)
	 * @see com.TwentyCodes.android.SkyHook.SkyHookService#onStartCommand(android.content.Intent, int, int)
	 * @author ricky barrette
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.mPeriod = (long) (60000 * Integer.parseInt(this.mSettings.getString(SettingsActivity.UPDATE_INTVERVAL , "10")));
		return super.onStartCommand(intent, flags, startId);
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
	
	/**
	 * starts a simple ongoing notification to inform the user that we are gathering location
	 * @author ricky barrette
	 */
	private void startOnGoingNotification() {
		this.mNotificationManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notifyDetails = new Notification(R.drawable.icon, this.getString(R.string.app_name), System.currentTimeMillis());
		PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, ListActivity.class), android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyDetails.setLatestEventInfo(this, this.getString(R.string.app_name), this.getString(R.string.gathering), intent);
		notifyDetails.flags |= Notification.FLAG_ONGOING_EVENT;
		this.mNotificationManager.notify(this.GATHERING_LOCATION_ONGING_NOTIFICATION_ID, notifyDetails);
	}
}