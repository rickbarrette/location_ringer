/**
 * LocationService.java
 * @date Jul 3, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.services;

import org.RickBarrette.android.LocationRinger.Constraints;
import org.RickBarrette.android.LocationRinger.R;
import org.RickBarrette.android.LocationRinger.ui.ListActivity;
import org.RickBarrette.android.LocationRinger.ui.SettingsActivity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import anroid.v4.compat.NotificationCompat;

import com.TwentyCodes.android.debug.LocationLibraryConstants;
import com.TwentyCodes.android.exception.ExceptionHandler;

/**
 * We override the location service so we can attach the exception handler
 * 
 * @author ricky barrette
 */
public class LocationService extends com.TwentyCodes.android.location.LocationService {

	/**
	 * convince method for getting the single shot service intent
	 * 
	 * @param context
	 * @return service intent
	 * @author ricky barrette
	 */
	public static Intent getSingleShotServiceIntent(final Context context) {
		return new Intent(context, LocationService.class).putExtra(LocationLibraryConstants.INTENT_EXTRA_REQUIRED_ACCURACY, Constraints.ACCURACY).setAction(				LocationLibraryConstants.INTENT_ACTION_UPDATE);
	}

	/**
	 * Starts the location service in multi shot mode
	 * 
	 * @param context
	 * @return
	 * @author ricky barrette
	 */
	public static void startMultiShotService(final Context context) {
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Constraints.UPDATE_INTERVAL, PendingIntent.getService(context, 0, getSingleShotServiceIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
	}

	/**
	 * starts the service in single shot mode
	 * 
	 * @param context
	 * @return
	 * @author ricky barrette
	 */
	public static ComponentName startSingleShotService(final Context context) {
		return context.startService(getSingleShotServiceIntent(context));
	}

	private final int GATHERING_LOCATION_ONGING_NOTIFICATION_ID = 232903877;

	private SharedPreferences mSettings;

	private NotificationManager mNotificationManager;

	private WifiManager mWifiManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		mSettings = getSharedPreferences(SettingsActivity.SETTINGS, Constraints.SHARED_PREFS_MODE);
		mSettings.edit().putBoolean(SettingsActivity.IS_SERVICE_STARTED, true).commit();
		startOnGoingNotification();
		
		/*
		 * enable wifi to aid in finding location.
		 * This will allow for faster location fixes
		 */
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWifiManager.setWifiEnabled(true);
		super.onCreate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onDestroy() {
		mSettings.edit().remove(SettingsActivity.IS_SERVICE_STARTED).commit();
		mNotificationManager.cancel(GATHERING_LOCATION_ONGING_NOTIFICATION_ID);
		
		/*
		 * disable wifi IF NOT connected to a network
		 */
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (! wifiNetInfo.isConnected())
			mWifiManager.setWifiEnabled(false);
		
		super.onDestroy();
	}

	/**
	 * starts a simple ongoing notification to inform the user that we are
	 * gathering location
	 * 
	 * @author ricky barrette
	 */
	private void startOnGoingNotification() {
		mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext()).setContentTitle(getString(R.string.app_name))
				.setContentText(this.getString(R.string.gathering)).setTicker(this.getString(R.string.gathering)).setSmallIcon(R.drawable.ic_stat_locationringer)
				.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, ListActivity.class), android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
				.setWhen(System.currentTimeMillis()).setOngoing(true);

		mNotificationManager.notify(GATHERING_LOCATION_ONGING_NOTIFICATION_ID, builder.getNotification());
	}
}