/**
 * MapFragment.java
 * @date Aug 8, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.TwentyCodes.android.LocationRinger.EnableScrollingListener;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.SearchRequestedListener;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.ui.SearchDialog;
import com.TwentyCodes.android.location.AndroidGPS;
import com.TwentyCodes.android.location.GeoPointLocationListener;
import com.TwentyCodes.android.location.OnLocationSelectedListener;
import com.TwentyCodes.android.overlays.RadiusOverlay;
import com.google.android.maps.GeoPoint;

/**
 * This fragment will be used to display and allow the user to edit the ringers
 * location trigger
 * 
 * @author ricky
 */
@SuppressLint("ValidFragment")
public class LocationInfomationFragment extends Fragment implements GeoPointLocationListener, OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener,
		OnLocationSelectedListener, SearchRequestedListener {

	private static final String TAG = "RingerInformationHowActivity";
	private final ContentValues mInfo;
	private final OnContentChangedListener mListener;
	private final EnableScrollingListener mEnableScrollingListener;
	private SeekBar mRadius;
	private MapFragment mMap;
	private ToggleButton mMapEditToggle;
	private RadiusOverlay mRadiusOverlay;
	private GeoPoint mPoint;
	private AndroidGPS mGPS;
	private View view;

	/**
	 * Creates a new MapFragment
	 * 
	 * @author ricky barrette
	 * @param ringerInformationActivity
	 */
	public LocationInfomationFragment(final ContentValues info, final OnContentChangedListener listener, final EnableScrollingListener enabledListener) {
		mInfo = info;
		mListener = listener;
		mEnableScrollingListener = enabledListener;
	}

	/**
	 * Called when a toggle button's state is changed
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
		view.findViewById(R.id.buttons).setVisibility(isChecked ? View.VISIBLE : View.GONE);

		if (mEnableScrollingListener != null)
			mEnableScrollingListener.setScrollEnabled(!isChecked);

		if (isChecked) {
			mGPS.enableLocationUpdates(this);
			mMap.enableGPSProgess();
		} else {
			mGPS.disableLocationUpdates();
			mMap.disableGPSProgess();
		}

		mMap.setDoubleTapZoonEnabled(isChecked);
		// buttons
		mMap.setBuiltInZoomControls(isChecked);
		mMap.setClickable(isChecked);
		mRadius.setEnabled(isChecked);
		Toast.makeText(getActivity(), isChecked ? getString(R.string.map_editing_enabled) : getString(R.string.map_editiing_disabled), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called when a view is clicked
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.mark_my_location:
			if (mPoint != null)
				onLocationSelected(mPoint);
			break;
		case R.id.my_location:
			if (mPoint != null)
				mMap.setMapCenter(mPoint);
			break;
		case R.id.map_mode:
			mMap.setSatellite(mMap.isSatellite() ? false : true);
			break;
		case R.id.search:
			new SearchDialog(getActivity(), this).show();
			break;
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.map_info_fragment, container, false);

		mGPS = new AndroidGPS(getActivity());

		mMap = (MapFragment) getFragmentManager().findFragmentById(R.id.mapview);
		mRadius = (SeekBar) view.findViewById(R.id.radius);
		mRadius.setMax(Debug.MAX_RADIUS_IN_METERS);
		mMap.setClickable(false);
		mMapEditToggle = (ToggleButton) view.findViewById(R.id.map_edit_toggle);
		mMapEditToggle.setChecked(false);
		mMapEditToggle.setOnCheckedChangeListener(this);
		mRadiusOverlay = new RadiusOverlay();
		mRadiusOverlay.setLocationSelectedListener(this);
		mRadius.setOnSeekBarChangeListener(this);
		mMap.addOverlay(mRadiusOverlay);
		mRadius.setEnabled(false);

		if (mInfo.get(RingerDatabase.KEY_LOCATION) != null) {
			final String[] point = mInfo.getAsString(RingerDatabase.KEY_LOCATION).split(",");
			mRadiusOverlay.setLocation(new GeoPoint(Integer.parseInt(point[0]), Integer.parseInt(point[1])));
		}

		if (mInfo.get(RingerDatabase.KEY_RADIUS) != null)
			mRadius.setProgress(mInfo.getAsInteger(RingerDatabase.KEY_RADIUS));

		if (mRadiusOverlay.getLocation() != null) {
			mMap.setMapCenter(mRadiusOverlay.getLocation());
			mMap.setZoom(16);
		}

		mMap.setDoubleTapZoonEnabled(false);

		view.findViewById(R.id.my_location).setOnClickListener(this);
		view.findViewById(R.id.mark_my_location).setOnClickListener(this);
		view.findViewById(R.id.search).setOnClickListener(this);
		view.findViewById(R.id.map_mode).setOnClickListener(this);

		return view;
	}

	/**
	 * Called when the location is a first fix (non-Javadoc)
	 * 
	 * @see com.TwentyCodes.android.location.GeoPointLocationListener#onFirstFix(boolean)
	 */
	@Override
	public void onFirstFix(final boolean isFirstFix) {
		if (mPoint != null){
			/*
			 * if this is the first fix and the radius overlay does not have a
			 * point specified then pan the map, and zoom in to the users
			 * current location
			 */
			if (isFirstFix) {
				mMap.disableGPSProgess();
				if (mRadiusOverlay.getLocation() == null)
					if (mMap != null) {
						mMap.setMapCenter(mPoint);
						mMap.setZoom(mMap.getMap().getMaxZoomLevel() - 5);
					}
			}
		} else
			mMap.enableGPSProgess();
	}

	/**
	 * Called when skyhook has a location to report
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onLocationChanged(final GeoPoint point, final int accuracy) {
		mPoint = point;
	}

	/**
	 * Called when a location has been selected (non-Javadoc)
	 * 
	 * @see com.TwentyCodes.android.location.OnLocationSelectedListener#onLocationSelected(com.google.android.maps.GeoPoint)
	 */
	@Override
	public void onLocationSelected(final GeoPoint point) {
		if (point != null) {
			if (Debug.DEBUG)
				Log.d(TAG, "onLocationSelected() " + point.toString());

			if (mRadiusOverlay != null)
				mRadiusOverlay.setLocation(point);

			if (mMap != null)
				mMap.setMapCenter(point);

			if (mListener != null) {
				final ContentValues info = new ContentValues();
				info.put(RingerDatabase.KEY_LOCATION, point.toString());
				mListener.onInfoContentChanged(info);
			}
		} else if (Debug.DEBUG)
			Log.d(TAG, "onLocationSelected() Location was null");
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		mGPS.disableLocationUpdates();
		super.onPause();
	}

	/**
	 * Called when a seekbar is has its progress changed
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
		switch (seekBar.getId()) {
		case R.id.radius:
			mRadiusOverlay.setRadius(progress);
			mMap.invalidate();
			if (mListener != null) {
				final ContentValues info = new ContentValues();
				info.put(RingerDatabase.KEY_RADIUS, progress);
				mListener.onInfoContentChanged(info);
			}
			break;
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		if (mMapEditToggle.isChecked())
			mGPS.enableLocationUpdates(this);
		super.onResume();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.TwentyCodes.android.LocationRinger.SearchRequestedListener#onSearchRequested()
	 */
	@Override
	public boolean onSearchRequested() {
		new SearchDialog(getActivity(), this).show();
		return true;
	}

	@Override
	public void onStartTrackingTouch(final SeekBar seekBar) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onDestroy()
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onStop() {
		mGPS.disableLocationUpdates();
		super.onDestroy();
	}

	@Override
	public void onStopTrackingTouch(final SeekBar seekBar) {
	}
}