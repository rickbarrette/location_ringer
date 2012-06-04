/**
 * SettingsActivity.java
 * @date May 4, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.TwentyCodes.android.LocationRinger.LocationRinger;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This is the settings activity for location ringer
 * @author ricky barrette
 */
public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {
	
	public static final String SETTINGS = "settings";
	public static final String UPDATE_INTVERVAL = "update_interval";
	public static final String IGNORE_LOCATION = "ignore_location";
	public static final String ACCURACY = "accuracy";
	public static final String TOASTY = "toasty";
	public static final String EMAIL = "email";
	public static final String START_ON_BOOT = "start_on_boot";
	public static final String IS_SERVICE_STARTED = "is_service_started";
	public static final String IS_FIRST_BOOT = "is_first_boot";
	public static final String IS_REGISTERED = "is_registered";
	public static final String IS_FIRST_RINGER_PROCESSING = "is_first_ringer_processing";
	public static final String IS_DEFAULT = "is_default";
	public static final String RESTORE = "restore";
	public static final String BACKUP = "backup";
	public static final String CURRENT = "current";
	
	/**
	 * Backs up the database
	 * @return true if successful
	 * @author ricky barrette
	 */
	public static boolean backup(final Context context){
		final File dbFile = new File(Environment.getDataDirectory() + "/data/"+context.getPackageName()+"/shared_prefs/"+SETTINGS+".xml");

		final File exportDir = new File(Environment.getExternalStorageDirectory(), "/"+context.getString(R.string.app_name));
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		final File file = new File(exportDir, dbFile.getName());

		try {
			file.createNewFile();
			copyFile(dbFile, file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Copies a file
	 * @param src file
	 * @param dst file
	 * @throws IOException
	 * @author ricky barrette
	 */
	private static void copyFile(final File src, final File dst) throws IOException {
        final FileChannel inChannel = new FileInputStream(src).getChannel();
        final FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
           inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
           if (inChannel != null)
              inChannel.close();
           if (outChannel != null)
              outChannel.close();
        }
     }
	
	/**
	 * Restores the database from the sdcard
	 * @return true if successful
	 * @author ricky barrette
	 */
	public static void restore(final Context context){
		final File dbFile = new File(Environment.getDataDirectory() + "/data/"+context.getPackageName()+"/shared_prefs/"+SETTINGS+".xml");

		final File exportDir = new File(Environment.getExternalStorageDirectory(), "/"+context.getString(R.string.app_name));
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		final File file = new File(exportDir, dbFile.getName());

		try {
			file.createNewFile();
			copyFile(file, dbFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		context.getSharedPreferences(SETTINGS, Debug.SHARED_PREFS_MODE).edit().remove(IS_FIRST_RINGER_PROCESSING).remove(IS_DEFAULT).remove(IS_SERVICE_STARTED).commit();
	}
	
	/**
	 * generates the exception repost email intent
	 * @param report
	 * @return intent to start users email client
	 * @author ricky barrette
	 */
	private Intent generateEmailIntent() {
		/*
		 * get the build information, and build the string
		 */
		final PackageManager pm = this.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException eNnf) {
			//doubt this will ever run since we want info about our own package
			pi = new PackageInfo();
			pi.versionName = "unknown";
			pi.versionCode = 1;
		}
		
		final Intent intent = new Intent(Intent.ACTION_SEND);
		final String theSubject = this.getString(R.string.app_name);
		final String theBody = "\n\n\n"+ Build.FINGERPRINT +"\n"+ this.getString(R.string.app_name)+" "+pi.versionName+" bulid "+pi.versionCode;
		intent.putExtra(Intent.EXTRA_EMAIL,new String[] {this.getString(R.string.email)});
		intent.putExtra(Intent.EXTRA_TEXT, theBody);
		intent.putExtra(Intent.EXTRA_SUBJECT, theSubject);
		intent.setType("message/rfc822");
		return intent;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onCreate(final Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.getPreferenceManager().setSharedPreferencesMode(Debug.SHARED_PREFS_MODE);
		this.getPreferenceManager().setSharedPreferencesName(SETTINGS);
		addPreferencesFromResource(R.xml.setings);
		this.findPreference(EMAIL).setOnPreferenceClickListener(this);
		
		/*
		 * Set up the action bar if required
		 */
		if(Debug.SUPPORTS_HONEYCOMB)
			getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * @author ricky barrette
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
	            final Intent intent = new Intent(this, LocationRinger.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra(ListActivity.NO_SPLASH, ListActivity.NO_SPLASH);
	            startActivity(intent);
	            return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * called when the email preference button is clicked
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {
		this.startActivity(generateEmailIntent());
		return false;
	}
}