/**
 * RingerProcessingService.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.services;

import java.util.Calendar;
import java.util.Map.Entry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.ui.SettingsActivity;
import com.TwentyCodes.android.debug.LocationLibraryConstants;
import com.TwentyCodes.android.exception.ExceptionHandler;
import com.TwentyCodes.android.location.GeoUtils;
import com.google.android.maps.GeoPoint;

/**
 * This service will handle processing the users location and the ringers
 * @author ricky barrette
 */
public class RingerProcessingService extends Service {

	private static final String TAG = "RingerProcessingService";
	private int mStartId;
	private Location mLocation;
	private RingerDatabase mDb;
	private WakeLock mWakeLock;
	private AudioManager mAudioManager;
	private SharedPreferences mSettings;
	private WifiManager mWifiManager;
	private BluetoothAdapter mBluetoothAdapter;
	
	/**
	 * Applies a ringers options to the current system settings
	 * @param id
	 * @author ricky barrette
	 */
	private void applyRinger(final ContentValues values) {
		if(Debug.DEBUG)
			Log.d(TAG, "applyRigner()");
		
		final String name = values.getAsString(RingerDatabase.KEY_RINGER_NAME);
		
		/*
		 * Make it toasty if the user wants to be notified.
		 * This will display a toast msg "Applying <ringer name>"
		 */
		if(this.getSharedPreferences(SettingsActivity.SETTINGS, Debug.SHARED_PREFS_MODE).getBoolean(SettingsActivity.TOASTY, false))
			Toast.makeText(this.getApplicationContext(), "Applying  "+ name, Toast.LENGTH_SHORT).show();
		
		/*
		 * We need to null check all the value except ring/notification tone uri's and boolean values.
		 */
		
		/*
		 * ringtone & volume
		 * if the ringtone is set to silent we want to set the volume to 0
		 */
		if(values.containsKey(RingerDatabase.KEY_RINGTONE_IS_SILENT)){
			Log.d(TAG, "Ringtone: "+ applyRingtone(RingtoneManager.TYPE_RINGTONE, RingerDatabase.parseBoolean(values.getAsString(RingerDatabase.KEY_RINGTONE_IS_SILENT)), values.getAsString(RingerDatabase.KEY_RINGTONE_URI)));		
			if(RingerDatabase.parseBoolean(values.getAsString(RingerDatabase.KEY_RINGTONE_IS_SILENT)))
				setStreamVolume(0, AudioManager.STREAM_RING);
			else
				if(values.get(RingerDatabase.KEY_RINGTONE_VOLUME) != null)
					setStreamVolume(values.getAsInteger(RingerDatabase.KEY_RINGTONE_VOLUME), AudioManager.STREAM_RING);
		}
	
		/*
		 * notification ringtone & volume
		 * if the notification ringtone is silent, we want to set the volume to 0
		 */
		if(values.containsKey(RingerDatabase.KEY_NOTIFICATION_IS_SILENT)){
			Log.d(TAG, "Notification Ringtone: "+ applyRingtone(RingtoneManager.TYPE_NOTIFICATION, RingerDatabase.parseBoolean(values.getAsString(RingerDatabase.KEY_NOTIFICATION_IS_SILENT)), values.getAsString(RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI)));
			if(RingerDatabase.parseBoolean(values.getAsString(RingerDatabase.KEY_NOTIFICATION_IS_SILENT)))
				setStreamVolume(0, AudioManager.STREAM_NOTIFICATION);
			else
				if(values.get(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME) != null)
					setStreamVolume(values.getAsInteger(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME), AudioManager.STREAM_NOTIFICATION);
		}
		
		if(Debug.DEBUG){
			Log.d(TAG, "Music "+ (mAudioManager.isMusicActive() ? "is playing " : "is not playing"));
			Log.d(TAG, "Wired Headset "+ (mAudioManager.isWiredHeadsetOn() ? "is on " : "is off"));
		}
		
		/*
		 * music volume
		 * we will set the music volume only if music is not playing, and there is no wired head set
		 */
		if(values.containsKey(RingerDatabase.KEY_MUSIC_VOLUME))
			if(values.get(RingerDatabase.KEY_MUSIC_VOLUME) != null)
				if(! mAudioManager.isMusicActive())
					if(! mAudioManager.isWiredHeadsetOn())
						setStreamVolume(values.getAsInteger(RingerDatabase.KEY_MUSIC_VOLUME), AudioManager.STREAM_MUSIC);
		
		/*
		 * alarm volume
		 * we will set the alarm volume only if music is not playing, and there is no wired head set
		 */
		if(values.containsKey(RingerDatabase.KEY_ALARM_VOLUME))
			if(values.get(RingerDatabase.KEY_ALARM_VOLUME) != null)
				if(! mAudioManager.isMusicActive())
					if(! mAudioManager.isWiredHeadsetOn())
						setStreamVolume(values.getAsInteger(RingerDatabase.KEY_ALARM_VOLUME), AudioManager.STREAM_ALARM);
		
		/*
		 * wifi & bluetooth 
		 */
		if(values.containsKey(RingerDatabase.KEY_WIFI))
			if(mWifiManager != null)
				mWifiManager.setWifiEnabled(RingerDatabase.parseBoolean(values.getAsString(RingerDatabase.KEY_WIFI)));
		
		if(values.containsKey(RingerDatabase.KEY_BT))
			if(mBluetoothAdapter != null)
				if(RingerDatabase.parseBoolean(values.getAsString(RingerDatabase.KEY_BT)))
					mBluetoothAdapter.enable();
				else
					mBluetoothAdapter.disable();
		
		/*
		 * update interval
		 */
		if(values.containsKey(RingerDatabase.KEY_UPDATE_INTERVAL))
			if (values.get(RingerDatabase.KEY_UPDATE_INTERVAL) != null){
				Intent i = new Intent(this, LocationService.class)
				.putExtra(LocationService.INTENT_EXTRA_REQUIRED_ACCURACY, Integer.parseInt(this.getSharedPreferences(SettingsActivity.SETTINGS, 2).getString(SettingsActivity.ACCURACY , "50")))
				.setAction(LocationLibraryConstants.INTENT_ACTION_UPDATE);
				PendingIntent pi = PendingIntent.getService(this, LocationService.REQUEST_CODE, i, 0);
				
				final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				
				/*
				 * cancel the existing schedule
				 */
				am.cancel(pi);
				
				/*
				 * reschedule the location service
				 */
				am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() +  (long) (60000 * Integer.parseInt(values.getAsString(RingerDatabase.KEY_UPDATE_INTERVAL))), pi);
			}
	}

	/**
	 * Apply the ring tone
	 * @param stream audio stream to apply to
	 * @param isSilent true if silent
	 * @param uri of ringtone, if null silent will be applied
	 * @return string uri of applied ringtone, null if silent was applied
	 * @author ricky barrette
	 */
	private String applyRingtone(int type, boolean isSilent, String uri) {
		String ringtoneURI = null;
		if(isSilent){
			if(Debug.DEBUG)
				Log.d(TAG, "Ringtone was set to silent ");
		} else
			ringtoneURI = uri;
		RingtoneManager.setActualDefaultRingtoneUri(this, type, ringtoneURI == null ? null : Uri.parse(ringtoneURI));
		return uri;
	}

//	/**
//	 * Backs up the current settings into the database to be re applied as default
//	 * @author ricky barrette
//	 */
//	private void backup() {
//		
//		if(Debug.DEBUG)
//			Log.d(TAG, "backup()");
//		
//		ContentValues ringer = new ContentValues();
//		ContentValues info = new ContentValues();
//		 
//		 /*
//		  * package the ringer table information
//		  */
//		 ringer.put(RingerDatabase.KEY_RINGER_NAME, DEFAULT_RINGER);
//		 
//		 /*
//		  * package the ringer_info table information
//		  */
//		 info.put(RingerDatabase.KEY_RINGTONE_IS_SILENT, mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ? true : false);
//		 info.put(RingerDatabase.KEY_RINGTONE_URI, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE).toString());
//		 info.put(RingerDatabase.KEY_NOTIFICATION_IS_SILENT, mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ? true : false);
//		 info.put(RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION).toString());
//		 info.put(RingerDatabase.KEY_RINGTONE_VOLUME, mAudioManager.getStreamVolume(AudioManager.STREAM_RING));
//		 info.put(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME, mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
//		 info.put(RingerDatabase.KEY_MUSIC_VOLUME, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
//		 info.put(RingerDatabase.KEY_ALARM_VOLUME, mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));
//		 info.put(RingerDatabase.KEY_UPDATE_INTERVAL, this.mSettings.getString(SettingsActivity.SETTINGS, "10"));
//		 
//		 if(this.mBluetoothAdapter != null)
//			 info.put(RingerDatabase.KEY_BT, this.mBluetoothAdapter.isEnabled());
//		 
//		 if(this.mWifiManager != null)
//			 info.put(RingerDatabase.KEY_WIFI, this.mWifiManager.isWifiEnabled());
//		 
//		 this.mDb.updateRinger(1, ringer, info);
//	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 * @author ricky barrette
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Called when the service is first created
	 * @author ricky barrette
	 */
	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		if(Debug.DEBUG)
			Log.d(TAG, "onCreate()");
		super.onCreate();
		this.mDb = new RingerDatabase(this);
		this.mSettings = this.getSharedPreferences(SettingsActivity.SETTINGS, Debug.SHARED_PREFS_MODE);
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = (WakeLock) pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		this.mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		this.mWifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        this.mWakeLock.acquire();
	}

	@Override
	public void onDestroy() {
		if(mWakeLock.isHeld())
			mWakeLock.release();
		System.gc();
		super.onDestroy();
	}

	/**
	 * Called when the service is first started
	 * @param intent
	 * @param flags
	 * @param startId
	 * @author ricky barrette
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(Debug.DEBUG)
			Log.d(TAG, "onStartCommand: "+startId);
		this.mStartId = startId;
		
		/*
		 * try to sleep so skyhook doesn't cock block us
		 */
		try {
			Thread.sleep(1000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
//		//if this is the first time the service ever starts, then back up the current settings to create a back up
//		if(this.mSettings.getBoolean(SettingsActivity.IS_FIRST_RINGER_PROCESSING, true)){
//			backup();
//			this.mSettings.edit().putBoolean(SettingsActivity.IS_FIRST_RINGER_PROCESSING, false).commit();
//		}
//		
//		//if the default ringer is the currently applied ringer back up the current settings
//		if(this.mSettings.getBoolean(SettingsActivity.IS_DEFAULT, false)){
//			backup();
//		}
		
		if(intent.getParcelableExtra(LocationLibraryConstants.INTENT_EXTRA_LOCATION_CHANGED) != null){
			this.mLocation = intent.getParcelableExtra(LocationLibraryConstants.INTENT_EXTRA_LOCATION_CHANGED);
			processRingers();
		}else if(Debug.DEBUG)
			Log.d(TAG, "Location was null");
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Processes the ringer database for applicable ringers
	 * @author ricky barrette
	 */
	private void processRingers() {
		long index = 1;
		boolean isDeafult = true;
		
		/*
		 * get the default ringer information
		 */
		final ContentValues ringer = getRinger(1);
		
		final GeoPoint point = new GeoPoint((int) (mLocation.getLatitude() * 1E6), (int) (mLocation.getLongitude()*1E6));
		if(Debug.DEBUG){
			Log.d(TAG, "Processing ringers");
			Log.d(TAG, "Current location "+(int) (mLocation.getLatitude() * 1E6)+", "+(int) (mLocation.getLongitude() * 1E6)+" @ "+  Float.valueOf(mLocation.getAccuracy()) / 1000+"km");
		}
		
		final Cursor c = mDb.getAllRingers();
		c.moveToFirst();
		if (c.moveToFirst()) {
			do {
				if(Debug.DEBUG)
					Log.d(TAG, "Checking ringer "+c.getString(0));
				
				if(RingerDatabase.parseBoolean(c.getString(1))){
					final ContentValues info = this.mDb.getRingerInfo(c.getString(0));
					if(info.containsKey(RingerDatabase.KEY_LOCATION) && info.containsKey(RingerDatabase.KEY_RADIUS)){
						final String[] pointInfo = info.getAsString(RingerDatabase.KEY_LOCATION).split(",");
						if(GeoUtils.isIntersecting(point, Float.valueOf(mLocation.getAccuracy()) / 1000, new GeoPoint(Integer.parseInt(pointInfo[0]), Integer.parseInt(pointInfo[1])), Float.valueOf(info.getAsInteger(RingerDatabase.KEY_RADIUS)) / 1000, Debug.FUDGE_FACTOR)){
							c.close();
							getRinger(ringer, index);
							isDeafult = false;
							//break loop, we will only apply the first applicable ringer
							break;
						}
					}
				}
				index++;
			} while (c.moveToNext());
		}
		
		c.close();
		
		if(Debug.DEBUG)
			for(Entry<String,Object> item : ringer.valueSet())
				Log.d(TAG, item.getKey());
		
		applyRinger(ringer);
		
		if(Debug.DEBUG)
			Log.d(TAG, "Finished processing ringers");
		
		//store is default
		this.mSettings.edit().putBoolean(SettingsActivity.IS_DEFAULT, isDeafult).commit();
			
		this.stopSelf(mStartId);
	}

	/**
	 * appends the new ringer's information in content values
	 * @param id
	 * @return
	 */
	private ContentValues getRinger(ContentValues values, long id) {
		String name = this.mDb.getRingerName(id);
		values.put(RingerDatabase.KEY_RINGER_NAME, name);
		
		/*
    	 * get the ringer's info, and parse it into content values
    	 */   	
    	values.putAll(this.mDb.getRingerInfo(name));
		return values;
	}
	
	/**
	 * returns all the ringers information as content values
	 * @param id
	 * @return
	 */
	private ContentValues getRinger(long id) {
		return getRinger(new ContentValues(), id);
	}

	/**
	 * set the volume of a particular stream
	 * @param volume
	 * @param stream
	 * @author ricky barrette
	 */
	private void setStreamVolume(int volume, int stream) {
		/*
		 * if the seek bar is set to a value that is higher than what the the stream value is set for
		 * then subtract the seek bar's value from the current volume of the stream, and then
		 * raise the stream by that many times 
		 */
		if (volume > mAudioManager.getStreamVolume(stream)) {
			int adjust = volume - mAudioManager.getStreamVolume(stream);
			for (int i = 0; i < adjust; i++) {
				mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE, stream, 0);
			}
		}
		
		/*
		 * if the seek bar is set to a value that is lower than what the the stream value is set for
		 * then subtract the current volume of the stream from the seek bar's value, and then
		 * lower the stream by that many times 
		 */
		if (volume < mAudioManager.getStreamVolume(stream)) {
			int adjust = mAudioManager.getStreamVolume(stream) - volume;
			for (int i = 0; i < adjust; i++) {
				mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER, stream, 0);
			}
		}		
	}
}