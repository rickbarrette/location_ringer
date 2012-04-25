/**
 * RingerInformationHowActivity.java
 * @date Jul 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.TwentyCodes.android.LocationRinger.LocationSelectedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.SkyHook.SkyHook;
import com.TwentyCodes.android.location.GeoPointLocationListener;
import com.TwentyCodes.android.location.MapView;
import com.google.android.maps.GeoPoint;

/**
 * This activity will allow users to pick how a ringer works. Using this activity they will pick triggers for this ringer
 * @author ricky
 */
public class HowActivity extends com.google.android.maps.MapActivity implements LocationSelectedListener, GeoPointLocationListener, OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener {

	private static final String TAG = "RingerInformationHowActivity";
	private static final int ADD_ID = 1;
	private static final int WHAT_REQUEST_CODE = 467468436;
	private SeekBar mRadius;
	private MapView mMapView;
	private ToggleButton mMapEditToggle;
	private RadiusOverlay mRadiusOverlay;
	private GeoPoint mPoint;
	private SkyHook mSkyHook;
	private ProgressDialog mGpsProgress;
	private boolean isFirstFix;
	private ScrollView mScrollView;

	@Override
	protected boolean isRouteDisplayed() {
		//UNUSED
		return false;
	}
	
	/**
	 * Called when the HOW activity finishes, this will just pass the results back to the ringer list activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			this.setResult(RESULT_OK, data);
			this.finish();
		}
		//don show for default
		if(this.getIntent().getBooleanExtra(ListActivity.KEY_IS_DEFAULT, false))
			this.finish();
	}

	/**
	 * Called when a toggle button's state is changed
	 * @author ricky barrette
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()){
				
			case R.id.map_edit_toggle:
				this.isFirstFix = isChecked;
				
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 
						isChecked 
							? (this.getResources().getDisplayMetrics().heightPixels -  findViewById(R.id.map_controls).getHeight()) 
								: (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics())));
				if(isChecked){
					this.mSkyHook.getUpdates();
					params.addRule(RelativeLayout.ALIGN_PARENT_TOP );
					this.mGpsProgress = ProgressDialog.show(this, "", this.getText(R.string.gps_fix), true, true);
				} else {
					this.mSkyHook.removeUpdates();
					params.addRule(RelativeLayout.BELOW, R.id.info);
					params.addRule(RelativeLayout.ALIGN_BOTTOM );
					if(this.mGpsProgress != null)
						this.mGpsProgress.dismiss();
				}
				findViewById(R.id.map_info).setLayoutParams(params );
				
				this.mMapView.setDoubleTapZoonEnabled(isChecked);
				//buttons
				findViewById(R.id.mark_my_location).setVisibility(isChecked ? View.VISIBLE : View.GONE);
				findViewById(R.id.my_location).setVisibility(isChecked ? View.VISIBLE : View.GONE);
				findViewById(R.id.map_mode).setVisibility(isChecked ? View.VISIBLE : View.GONE);
				findViewById(R.id.search).setVisibility(isChecked ? View.VISIBLE : View.GONE);
				findViewById(R.id.add_feature_button).setVisibility(isChecked ? View.GONE : View.VISIBLE);
				this.mScrollView.invalidate();
				this.mScrollView.setScrollEnabled(! isChecked);
				this.mMapView.setBuiltInZoomControls(isChecked);
				this.mMapView.setClickable(isChecked);
				this.mRadius.setEnabled(isChecked);
				Toast.makeText(this, isChecked ? getString(R.string.map_editing_enabled) : getString(R.string.map_editiing_disabled), Toast.LENGTH_SHORT).show();
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
			case R.id.mark_my_location:
				if(this.mPoint != null){
					this.mRadiusOverlay.setLocation(mPoint);
					this.mMapView.getController().setCenter(mPoint);
				}
				break;
			case R.id.my_location:
				if(this.mPoint != null)
					this.mMapView.getController().setCenter(mPoint);
				break;
			case R.id.map_mode:
				this.mMapView.setSatellite(mMapView.isSatellite() ? false : true);
				break;
			case R.id.search:
				new SearchDialog(this, this).show();
				break;
			case R.id.add_feature_button:
				Toast.makeText(this, "NO TRIGGERS YET", Toast.LENGTH_LONG).show();
				break;
			case R.id.save_ringer_button:
				save();
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
		this.setContentView(R.layout.how);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		this.mSkyHook = new SkyHook(this);
		this.mSkyHook.setLocationListener(this);
		
		this.mScrollView = (ScrollView) findViewById(R.id.scrollview);
		
		this.mMapView = (MapView) findViewById(R.id.mapview);
		this.mRadius = (SeekBar) findViewById(R.id.radius);
		this.mRadius.setMax(Debug.MAX_RADIUS_IN_METERS);
		this.mMapView.setClickable(false);
		this.mMapEditToggle = (ToggleButton) findViewById(R.id.map_edit_toggle);
		this.mMapEditToggle.setChecked(false);
		this.mMapEditToggle.setOnCheckedChangeListener(this);
		this.mRadiusOverlay = new RadiusOverlay();
		this.mRadius.setOnSeekBarChangeListener(this);
		this.mMapView.getOverlays().add(mRadiusOverlay);
		this.mRadius.setEnabled(false);
		
		findViewById(R.id.mark_my_location).setOnClickListener(this);
		findViewById(R.id.my_location).setOnClickListener(this);
		findViewById(R.id.map_mode).setOnClickListener(this);
		findViewById(R.id.search).setOnClickListener(this);
		findViewById(R.id.save_ringer_button).setOnClickListener(this);
		findViewById(R.id.add_feature_button).setOnClickListener(this);
		
		
		Intent data = this.getIntent();

		this.setTitle(getString(R.string.editing)+" "+data.getStringExtra(RingerDatabase.KEY_RINGER_NAME));
		
		if(data.hasExtra(ListActivity.KEY_INFO)){
			/*
			 * We need to null check all the values 
			 */			
			ContentValues info = (ContentValues) data.getParcelableExtra(ListActivity.KEY_INFO);
			
			if (info.get(RingerDatabase.KEY_LOCATION_LAT) != null && info.get(RingerDatabase.KEY_LOCATION_LON) != null){
				this.mRadiusOverlay.setLocation(new GeoPoint(info.getAsInteger(RingerDatabase.KEY_LOCATION_LAT), info.getAsInteger(RingerDatabase.KEY_LOCATION_LON)));
			}
				
			if (info.get(RingerDatabase.KEY_RADIUS) != null){
				this.mRadius.setProgress(info.getAsInteger(RingerDatabase.KEY_RADIUS));
			}
			
			if(data.getBooleanExtra(ListActivity.KEY_IS_DEFAULT, false)){
				this.startActivityForResult(new Intent(this, WhatActivity.class).putExtras(this.getIntent()), WHAT_REQUEST_CODE);
			}
		} else
			this.setTitle(R.string.new_ringer);
		
		if(this.mRadiusOverlay.getLocation() != null){
			this.mMapView.getController().setCenter(this.mRadiusOverlay.getLocation());
			this.mMapView.getController().setZoom(16);
		}
		
		
		this.mMapView.setDoubleTapZoonEnabled(false);
	}

	/**
	 * Creates the main menu that is displayed when the menu button is clicked
	 * @author ricky barrette
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_ID, 0, getString(R.string.add_feature)).setIcon(android.R.drawable.ic_menu_add);
		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onDestroy()
	 * @author ricky barrette
	 */
	@Override
	protected void onDestroy() {
		this.mSkyHook.removeUpdates();
		super.onDestroy();
	}

	/**
	 * Called when skyhook has a location to report
	 * @author ricky barrette
	 */
	@Override
	public void onLocationChanged(GeoPoint point, int accuracy) {
		this.mPoint = point;
		
		if(point != null){
			
			/*
			 * if this is the first fix and the radius overlay does not have a point specified
			 * then pan the map, and zoom in to the users current location
			 */
			if(this.isFirstFix)
				if(this.mRadiusOverlay.getLocation() == null){
					if(this.mMapView != null){
						this.mMapView.getController().setCenter(point);
						this.mMapView.getController().setZoom((this.mMapView.getMaxZoomLevel() - 5));
					}
					this.isFirstFix = false;
				}
			
			/*
			 * dismiss the acquiring gps dialog
			 */
			if(this.mGpsProgress != null)
				this.mGpsProgress.dismiss();
		}
	}

	/*
	 */
	@Override
	public void onLocationSelected(GeoPoint point) {
		if(point != null){
			if(Debug.DEBUG)
				Log.d(TAG, "onLocationSelected() "+ point.toString());

			if(this.mRadiusOverlay != null)
				this.mRadiusOverlay.setLocation(point);
			
			if(this.mMapView != null){
				this.mMapView.getController().setCenter(point);
				this.mMapView.getController().setZoom((this.mMapView.getMaxZoomLevel() - 5));
			}
		} else if(Debug.DEBUG)
			Log.d(TAG, "onLocationSelected() Location was null");
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * @author ricky barrette
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case ADD_ID:
				//TODO display triggers
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Called when a seekbar is has its progress changed
	 * @author ricky barrette
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch (seekBar.getId()){
			case R.id.radius:
				this.mRadiusOverlay.setRadius(progress);
				this.mMapView.invalidate();
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//UNUSED
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//UNUSED
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
				 
				 Intent data = new Intent(HowActivity.this, WhatActivity.class).putExtras(HowActivity.this.getIntent());
				 GeoPoint point = HowActivity.this.mRadiusOverlay.getLocation();
				 
				 ContentValues info = data.getParcelableExtra(ListActivity.KEY_INFO);
				 
				 if(info == null)
					 info = new ContentValues();
				 /*
				  * package the ringer table information
				  */
				 info.put(RingerDatabase.KEY_LOCATION_LAT, point.getLatitudeE6());
				 info.put(RingerDatabase.KEY_LOCATION_LON, point.getLongitudeE6());
				 info.put(RingerDatabase.KEY_RADIUS, HowActivity.this.mRadius.getProgress());
				 
				 //package the intent
				 data.putExtra(ListActivity.KEY_INFO, info);
				 progress.dismiss();
				 HowActivity.this.startActivityForResult(data, WHAT_REQUEST_CODE);
			 }
		 }).start();
	}

	@Override
	public void onFirstFix(boolean isFirstFix) {
		// TODO Auto-generated method stub
		
	}

}