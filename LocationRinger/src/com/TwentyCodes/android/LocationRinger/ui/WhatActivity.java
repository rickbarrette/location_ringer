/**

 * RingeInformationActivity.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;

/**
 * This activity will be used to display what this ringer controls. here the user will be able to add or modify features they wish to control
 * @author ricky barrette
 */
public class WhatActivity extends Activity implements OnCheckedChangeListener, OnClickListener {

	private static final int SAVE_ID = 0;
	//private static final String TAG = "RingerInformationWhatActivity";
	private static final int ADD_ID = 1;
	private SeekBar mRingtonVolume;
	private SeekBar mNotificationRingtoneVolume;
	private EditText mNotificationRingtone;
	private EditText mRingerName;
	private EditText mRingtone;
	private ToggleButton mNotificationRingtoneToggle;
	private ToggleButton mRingerToggle;
	private ToggleButton mRingtoneToggle;
	private String mRingtoneURI;
	private String mNotificationRingtoneURI;
	private ToggleButton mWifiToggle;
	private ToggleButton mBTToggle;
	private SeekBar mAlarmVolume;
	private ProgressBar mMusicVolume;
	
	private void addFeature(int item) {
		String feature = this.getResources().getStringArray(R.array.features)[item];
		
		if(feature.equals(this.getString(R.string.ringtone))){
			findViewById(R.id.ringtone_info).setVisibility(View.VISIBLE);
		}
		
		if(feature.equals(this.getString(R.string.notification_ringtone))){
			findViewById(R.id.notification_ringtone_info).setVisibility(View.VISIBLE);
		}
		
		if(feature.equals(this.getString(R.string.alarm_volume))){
			findViewById(R.id.alarm_volume_info).setVisibility(View.VISIBLE);
		}
		
		if(feature.equals(this.getString(R.string.music_volume))){
			findViewById(R.id.music_volume_info).setVisibility(View.VISIBLE);
		}
		
		if(feature.equals(this.getString(R.string.bluetooth))){
			findViewById(R.id.bluetooth_toggle).setVisibility(View.VISIBLE);
		}
		
		if(feature.equals(this.getString(R.string.wifi))){
			findViewById(R.id.wifi_toggle).setVisibility(View.VISIBLE);
		}
		
		if(feature.equals(this.getString(R.string.update_interval))){
			findViewById(R.id.update_interval_info).setVisibility(View.VISIBLE);
		}
		
		if(this.mBTToggle.isShown() || this.mWifiToggle.isShown())
			findViewById(R.id.data_label).setVisibility(View.VISIBLE);
	}
	
	/**
	 * Will display a prompt asking for what feature to add.
	 * @author ricky
	 */
	private void displayFeaturesDialog() {
		
		/*
		 * TODO
		 * Check to see if wifi is available
		 * check to see if bluetooth is available,
		 * remove unavailable options
		 */
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getText(R.string.add_feature));
		builder.setItems(R.array.features, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        addFeature(item);
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	/**
	 * starts the ringtone picker 
	 * @param ringtoneCode RingtoneManager.TYPE_?
	 * @param uri of current tone
	 * @author ricky barrette
	 */
	private void getRingtoneURI(int ringtoneCode, String uri){
        Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneCode);
        intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        if( uri == null)
			try {
				uri = RingtoneManager.getActualDefaultRingtoneUri(this, ringtoneCode).toString();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri == null ? null : Uri.parse(uri));
        startActivityForResult( intent, ringtoneCode);  
	}
	
	/**
	 * Called when the rintone picker activity returns it's result
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case RingtoneManager.TYPE_RINGTONE:
					this.mRingtoneURI = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI).toString();
					Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(this.mRingtoneURI));
					this.mRingtone.setText(ringtone == null ? "Silent" : ringtone.getTitle(this));
					break;
				case RingtoneManager.TYPE_NOTIFICATION:
					this.mNotificationRingtoneURI = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI).toString();
					Ringtone notificationTone = RingtoneManager.getRingtone(this, Uri.parse(this.mNotificationRingtoneURI));
					this.mNotificationRingtone.setText(notificationTone == null ? "Silent" : notificationTone.getTitle(this));
					break;
			}
		}
	}

	/**
	 * Called when a toggle button's state is changed
	 * @author ricky barrette
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()){
			case R.id.ringer_toggle:
				//TODO disable all the child views, or find xml tag to allow setting of child view enabled via parent 
				findViewById(R.id.ringtone_info).setEnabled(isChecked);
				findViewById(R.id.notification_ringtone_info).setEnabled(isChecked);
				findViewById(R.id.alarm_volume_info).setEnabled(isChecked);
				findViewById(R.id.music_volume_info).setEnabled(isChecked);
				findViewById(R.id.bluetooth_toggle).setEnabled(isChecked);
				findViewById(R.id.wifi_toggle).setEnabled(isChecked);
				findViewById(R.id.update_interval_info).setEnabled(isChecked);
				findViewById(R.id.data_label).setEnabled(isChecked);
				break;
				
			case R.id.notification_silent_toggle:
				findViewById(R.id.notification_ringtone_button).setEnabled(!isChecked);
				this.mNotificationRingtone.setEnabled(!isChecked);
				this.mNotificationRingtoneVolume.setEnabled(!isChecked);
				break;
			
			case R.id.ringtone_silent_toggle:
				this.mRingtone.setEnabled(!isChecked);
				findViewById(R.id.ringtone_button).setEnabled(!isChecked);
				this.mRingtonVolume.setEnabled(!isChecked);
				break;
		}
	}

	/**
	 * Called when a view is clicked
	 * @author ricky barrette
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.notification_ringtone_button:
				getRingtoneURI(RingtoneManager.TYPE_NOTIFICATION, mNotificationRingtoneURI);
				break;
			case R.id.ringtone_button:
				getRingtoneURI(RingtoneManager.TYPE_RINGTONE, mRingtoneURI);
				break;
			case R.id.save_ringer_button:
				save();
				break;
			case R.id.add_feature_button:
				findViewById(R.id.add_a_feature_label).setVisibility(View.GONE);
				displayFeaturesDialog();
				break;
				
		}
	}

	/**
	 * Called when the acivity is first created
	 * @author ricky barrette
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.what);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		this.mRingerName = (EditText) findViewById(R.id.ringer_name);
		this.mRingerToggle = (ToggleButton) findViewById(R.id.ringer_toggle);
		this.mRingerToggle.setChecked(true);
		this.mRingerToggle.setOnCheckedChangeListener(this);
		
		this.mRingtone = (EditText) findViewById(R.id.ringtone);
		this.mRingtoneToggle = (ToggleButton) findViewById(R.id.ringtone_silent_toggle);
		this.mRingtonVolume = (SeekBar) findViewById(R.id.ringtone_volume);
		this.mRingtonVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
		this.mRingtonVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_RING));
		this.mRingtoneToggle.setChecked(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ? true : false);
		this.mRingtoneToggle.setOnCheckedChangeListener(this);
		this.mRingtone.setEnabled(! mRingtoneToggle.isChecked());
		this.mRingtonVolume.setEnabled(! mRingtoneToggle.isChecked());
		this.mRingtone.setClickable(true);
		
		this.mNotificationRingtone = (EditText) findViewById(R.id.notification_ringtone);
		this.mNotificationRingtoneVolume = (SeekBar) findViewById(R.id.notification_ringtone_volume);
		this.mNotificationRingtoneToggle = (ToggleButton) findViewById(R.id.notification_silent_toggle);
		this.mNotificationRingtoneVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
		this.mNotificationRingtoneVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
		this.mNotificationRingtoneToggle.setOnCheckedChangeListener(this);
		this.mNotificationRingtoneToggle.setChecked(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ? true : false);
		this.mNotificationRingtone.setEnabled(! mNotificationRingtoneToggle.isChecked());
		this.mNotificationRingtoneVolume.setEnabled(! mNotificationRingtoneToggle.isChecked());
		this.mNotificationRingtone.setClickable(true);
		
		this.mMusicVolume = (SeekBar) findViewById(R.id.music_volume);
		this.mMusicVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		this.mMusicVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		
		this.mAlarmVolume = (SeekBar) findViewById(R.id.alarm_volume);
		this.mAlarmVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
		this.mAlarmVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM));
		
		this.mWifiToggle = (ToggleButton) findViewById(R.id.wifi_toggle);
		this.mBTToggle = (ToggleButton) findViewById(R.id.bluetooth_toggle);
		
		findViewById(R.id.ringtone_button).setOnClickListener(this);
		findViewById(R.id.notification_ringtone_button).setOnClickListener(this);
		findViewById(R.id.save_ringer_button).setOnClickListener(this);
		findViewById(R.id.mark_my_location).setOnClickListener(this);
		findViewById(R.id.my_location).setOnClickListener(this);
		findViewById(R.id.map_mode).setOnClickListener(this);
		findViewById(R.id.search).setOnClickListener(this);
		findViewById(R.id.add_feature_button).setOnClickListener(this);
		
		try {
			this.mRingtoneURI = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE).toString();
			this.mRingtone.setText(RingtoneManager.getRingtone(this, Uri.parse(mRingtoneURI)).getTitle(this));
		} catch (NullPointerException e) {
			e.printStackTrace();
			this.mRingtoneToggle.setChecked(true);
		}
		
		try {
			this.mNotificationRingtoneURI = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION).toString();
			this.mNotificationRingtone.setText(RingtoneManager.getRingtone(this, Uri.parse(mNotificationRingtoneURI)).getTitle(this));
		} catch (NullPointerException e) {
			e.printStackTrace();
			this.mNotificationRingtoneToggle.setChecked(true);
		}
		
		Intent data = this.getIntent();
		
		if(data.hasExtra(ListActivity.KEY_INFO)){
			
			this.mRingerToggle.setChecked(data.getBooleanExtra(RingerDatabase.KEY_IS_ENABLED, true));
			this.mRingerName.setText(data.getStringExtra(RingerDatabase.KEY_RINGER_NAME));
			this.setTitle(getString(R.string.editing)+" "+mRingerName.getText().toString());
			
			/*
			 * if this is the default ringer, then we will display everything
			 */
			if(data.getBooleanExtra(ListActivity.KEY_IS_DEFAULT, false)){
				findViewById(R.id.ringer_options).setVisibility(View.GONE);
				findViewById(R.id.notification_ringtone_info).setVisibility(View.VISIBLE);
				findViewById(R.id.ringtone_info).setVisibility(View.VISIBLE);
				findViewById(R.id.wifi_toggle).setVisibility(View.VISIBLE);
				findViewById(R.id.data_label).setVisibility(View.VISIBLE);
				findViewById(R.id.bluetooth_toggle).setVisibility(View.VISIBLE);
				findViewById(R.id.music_volume_info).setVisibility(View.VISIBLE);
				findViewById(R.id.alarm_volume_info).setVisibility(View.VISIBLE);
				findViewById(R.id.update_interval_info).setVisibility(View.VISIBLE);
				findViewById(R.id.add_feature_button).setVisibility(View.GONE);
			}

			/*
			 * We need to null check all the values 
			 */			
			ContentValues info = (ContentValues) data.getParcelableExtra(ListActivity.KEY_INFO);
			
			if(RingerDatabase.parseBoolean(info.getAsString(RingerDatabase.KEY_PLUS_BUTTON_HINT)));
				findViewById(R.id.add_a_feature_label).setVisibility(View.GONE);

			if (info.get(RingerDatabase.KEY_NOTIFICATION_IS_SILENT) != null){
				this.mNotificationRingtoneToggle.setChecked(RingerDatabase.parseBoolean(info.getAsString(RingerDatabase.KEY_NOTIFICATION_IS_SILENT)));
				findViewById(R.id.notification_ringtone_info).setVisibility(View.VISIBLE);
			}
			
			if (info.get(RingerDatabase.KEY_NOTIFICATION_RINGTONE) != null){
				this.mNotificationRingtone.setText(info.getAsString(RingerDatabase.KEY_NOTIFICATION_RINGTONE));
				findViewById(R.id.notification_ringtone_info).setVisibility(View.VISIBLE);
			}
			
			if (info.get(RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI) != null)
				this.mNotificationRingtoneURI = info.getAsString(RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI);
			
			if (info.get(RingerDatabase.KEY_RINGTONE) != null){
				findViewById(R.id.ringtone_info).setVisibility(View.VISIBLE);
				this.mRingtone.setText(info.getAsString(RingerDatabase.KEY_RINGTONE));
			}
			
			if (info.get(RingerDatabase.KEY_RINGTONE_IS_SILENT) != null) {
				this.mRingtoneToggle.setChecked(RingerDatabase.parseBoolean(info.getAsString(RingerDatabase.KEY_RINGTONE_IS_SILENT)));
				findViewById(R.id.ringtone_info).setVisibility(View.VISIBLE);
			}
			
			if (info.get(RingerDatabase.KEY_RINGTONE_URI) != null)
				this.mRingtoneURI = info.getAsString(RingerDatabase.KEY_RINGTONE_URI);
			
			if (info.get(RingerDatabase.KEY_WIFI) != null){
				this.mWifiToggle.setChecked(RingerDatabase.parseBoolean(info.getAsString(RingerDatabase.KEY_WIFI)));
				findViewById(R.id.wifi_toggle).setVisibility(View.VISIBLE);
				findViewById(R.id.data_label).setVisibility(View.VISIBLE);
			}
			
			if (info.get(RingerDatabase.KEY_BT) != null){
				this.mBTToggle.setChecked(RingerDatabase.parseBoolean(info.getAsString(RingerDatabase.KEY_BT)));
				findViewById(R.id.bluetooth_toggle).setVisibility(View.VISIBLE);
				findViewById(R.id.data_label).setVisibility(View.VISIBLE);
			}
			
			if (info.get(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME) != null)
				this.mNotificationRingtoneVolume.setProgress(info.getAsInteger(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME));
			
			if (info.get(RingerDatabase.KEY_RINGTONE_VOLUME) != null)
				this.mRingtonVolume.setProgress(info.getAsInteger(RingerDatabase.KEY_RINGTONE_VOLUME));
			
			if (info.get(RingerDatabase.KEY_MUSIC_VOLUME) != null){
				this.mMusicVolume.setProgress(info.getAsInteger(RingerDatabase.KEY_MUSIC_VOLUME));
				findViewById(R.id.music_volume_info).setVisibility(View.VISIBLE);
			}
			
			if (info.get(RingerDatabase.KEY_ALARM_VOLUME) != null){
				this.mAlarmVolume.setProgress(info.getAsInteger(RingerDatabase.KEY_ALARM_VOLUME));
				findViewById(R.id.alarm_volume_info).setVisibility(View.VISIBLE);
			}
			
			if (info.get(RingerDatabase.KEY_UPDATE_INTERVAL) != null){
				String ui = info.getAsString(RingerDatabase.KEY_UPDATE_INTERVAL);
				findViewById(R.id.update_interval_info).setVisibility(View.VISIBLE);
				String[] values = this.getResources().getStringArray(R.array.runtimes);
				for(int i = 0; i < values.length; i++)
					if(ui.equals(values[i])){
						((Spinner) findViewById(R.id.update_interval)).setSelection(i);
						break;
					}
			}
			
		} else
			this.setTitle(R.string.new_ringer);
		
	}

	/**
	 * Creates the main menu that is displayed when the menu button is clicked
	 * @author ricky barrette
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SAVE_ID, 0, getString(R.string.save_ringer)).setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, ADD_ID, 0, getString(R.string.add_feature)).setIcon(android.R.drawable.ic_menu_add);
		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * @author ricky barrette
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case ADD_ID:
				displayFeaturesDialog();
				break;
			case SAVE_ID:
				save();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Prepares a bundle containing all the information that needs to be saved, and returns it to the starting activity
	 * @author ricky barrette
	 */
	private void save() {
		final ProgressDialog progress = ProgressDialog.show(this, "", this.getText(R.string.saving), true, true);
		
		//Generate the intent in a thread to prevent anr's and allow for progress dialog
		 new Thread( new Runnable(){
			 @Override
			 public void run(){
				 Looper.prepare();
				 
				 Intent data = new Intent().putExtras(WhatActivity.this.getIntent());
				 
				 ContentValues ringer = WhatActivity.this.getIntent().getParcelableExtra(ListActivity.KEY_RINGER);
				 ContentValues info = WhatActivity.this.getIntent().getParcelableExtra(ListActivity.KEY_INFO);
				 
				 if(ringer == null)
					 ringer = new ContentValues();
				 
				 if(info == null)
					 info = new ContentValues();
				 
				 /*
				  * package the ringer table information
				  */
				 ringer.put(RingerDatabase.KEY_RINGER_NAME, WhatActivity.this.mRingerName.getText().toString());
				 ringer.put(RingerDatabase.KEY_IS_ENABLED, WhatActivity.this.mRingerToggle.isChecked());
				 
				 /*
				  * package the ringer_info table information
				  */
				 if(findViewById(R.id.notification_ringtone_info).isShown()){
					 info.put(RingerDatabase.KEY_NOTIFICATION_IS_SILENT, WhatActivity.this.mNotificationRingtoneToggle.isChecked());
					 info.put(RingerDatabase.KEY_NOTIFICATION_RINGTONE, WhatActivity.this.mNotificationRingtone.getText().toString());
					 info.put(RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI, WhatActivity.this.mNotificationRingtoneURI);
					 info.put(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME, WhatActivity.this.mNotificationRingtoneVolume.getProgress());
				 }
				 
				 if(findViewById(R.id.ringtone_info).isShown()){
					 info.put(RingerDatabase.KEY_RINGTONE, WhatActivity.this.mRingtone.getText().toString());
					 info.put(RingerDatabase.KEY_RINGTONE_IS_SILENT, WhatActivity.this.mRingtoneToggle.isChecked());
					 info.put(RingerDatabase.KEY_RINGTONE_URI, WhatActivity.this.mRingtoneURI);
					 info.put(RingerDatabase.KEY_RINGTONE_VOLUME, WhatActivity.this.mRingtonVolume.getProgress());
				 }
				 
				 if(findViewById(R.id.wifi_toggle).isShown())
					 info.put(RingerDatabase.KEY_WIFI, WhatActivity.this.mWifiToggle.isChecked());
				 
				 if(findViewById(R.id.bluetooth_toggle).isShown())
					 info.put(RingerDatabase.KEY_BT, WhatActivity.this.mBTToggle.isChecked());
				 
				 if(findViewById(R.id.music_volume_info).isShown())
					 info.put(RingerDatabase.KEY_MUSIC_VOLUME, WhatActivity.this.mMusicVolume.getProgress());
				 
				 if(findViewById(R.id.alarm_volume_info).isShown())
					 info.put(RingerDatabase.KEY_ALARM_VOLUME, WhatActivity.this.mAlarmVolume.getProgress());
				 
				 if(findViewById(R.id.update_interval_info).isShown())
					 info.put(RingerDatabase.KEY_UPDATE_INTERVAL, 
							 WhatActivity.this.getResources().getStringArray(R.array.runtimes)[((Spinner) findViewById(R.id.update_interval)).getSelectedItemPosition()]);
				 
				 info.put(RingerDatabase.KEY_PLUS_BUTTON_HINT, true);
				 
				 //package the intent
				 data.putExtra(ListActivity.KEY_RINGER, ringer).putExtra(ListActivity.KEY_INFO, info);
				 
				 WhatActivity.this.setResult(RESULT_OK, data);
				 progress.dismiss();
				 WhatActivity.this.finish();
			 }
		 }).start();
	}
}