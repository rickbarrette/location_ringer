/**
 * MapFragment.java
 * @date Aug 8, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.ui.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.TwentyCodes.android.location.AndroidGPS;
import com.TwentyCodes.android.location.GeoUtils;
import com.TwentyCodes.android.location.LatLngListener;
import com.TwentyCodes.android.location.OnLocationSelectedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import org.RickBarrette.android.LocationRinger.*;
import org.RickBarrette.android.LocationRinger.db.RingerDatabase;
import org.RickBarrette.android.LocationRinger.ui.SearchDialog;

/**
 * This fragment will be used to display and allow the user to edit the ringers
 * location trigger
 * 
 * @author ricky
 */
@SuppressLint("ValidFragment")
public class LocationInformationFragment extends Fragment implements LatLngListener, OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener, SearchRequestedListener, GoogleMap.OnMapClickListener, OnLocationSelectedListener, GoogleMap.OnMarkerDragListener {

	private static final String TAG = "RingerInformationHowActivity";
	private final ContentValues mInfo;
	private final OnContentChangedListener mListener;
	private final EnableScrollingListener mEnableScrollingListener;
	private SeekBar mRadius;
	private GoogleMap mMap;
	private ToggleButton mMapEditToggle;
	private LatLng mPoint;
	private AndroidGPS mGPS;
	private View view;
	private TextView mRadiusTextView;
	private Circle mCircle;
	private ProgressBar mProgress;
	private Marker mMarker;


	/**
	 * Creates a new MapFragment
	 * 
	 * @author ricky barrette
	 */
	public LocationInformationFragment(final ContentValues info, final OnContentChangedListener listener, final EnableScrollingListener enabledListener) {
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
			mProgress.setVisibility(View.VISIBLE);
			mMap.setOnMapClickListener(this);
		} else {
			mGPS.disableLocationUpdates();
			mProgress.setVisibility(View.INVISIBLE);
			mMap.setOnMapClickListener(null);
		}

		enableMap(isChecked);
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
				onMapClick(mPoint);
			break;
		case R.id.my_location:
			if (mPoint != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLng(mPoint));
			break;
		case R.id.map_mode:
			mMap.setMapType(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.search:
			new SearchDialog(getActivity(), this).show();
			break;
		}
	}

	/**
	 * Called when the fragment view is being created
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.map_info_fragment, container, false);
		setUpMapIfNeeded();

		mGPS = new AndroidGPS(getActivity());

		mRadius = (SeekBar) view.findViewById(R.id.radius);
		mRadiusTextView = (TextView) view.findViewById(R.id.radius_textview);
		mRadius.setMax(Constraints.MAX_RADIUS_IN_METERS);

		mMapEditToggle = (ToggleButton) view.findViewById(R.id.map_edit_toggle);
		mMapEditToggle.setChecked(false);
		mMapEditToggle.setOnCheckedChangeListener(this);

		mRadius.setOnSeekBarChangeListener(this);
		mRadius.setEnabled(false);

		mProgress = (ProgressBar) view.findViewById(R.id.map_progress);

		if (mInfo.get(RingerDatabase.KEY_RADIUS) != null) {
			mRadius.setProgress(mInfo.getAsInteger(RingerDatabase.KEY_RADIUS));
		}

		if (mInfo.get(RingerDatabase.KEY_LOCATION) != null) {
			final String[] point = mInfo.getAsString(RingerDatabase.KEY_LOCATION).split(",");
			final LatLng location = new LatLng(Double.parseDouble(point[0]), Double.parseDouble(point[1]));

			mCircle.setCenter(location);
			mMarker.setPosition(location);

			mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
				@Override
				public void onMapLoaded() {
					if(Constraints.DEBUG)
						Log.d(TAG,"onMapLoaded()");
					final LatLngBounds.Builder builder = LatLngBounds.builder();
					builder.include(location);
					builder.include(GeoUtils.distanceFrom(location, mRadius.getProgress(), 90));
					mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),25, 25, 5));
//					mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
					if(Constraints.DEBUG){
						final MarkerOptions marker = new MarkerOptions();
						marker.position(location);
						marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
						mMap.addMarker(marker);
					}

				}
			});

		}

		view.findViewById(R.id.my_location).setOnClickListener(this);
		view.findViewById(R.id.mark_my_location).setOnClickListener(this);
		view.findViewById(R.id.search).setOnClickListener(this);
		view.findViewById(R.id.map_mode).setOnClickListener(this);

		return view;
	}

	/**
	 * Called when the location is a first fix (non-Javadoc)
	 *
	 * @see com.TwentyCodes.android.location.LatLngListener#onFirstFix(boolean)
	 */
	@Override
	public void onFirstFix(final boolean isFirstFix) {
		if (mPoint != null) {
			/*
			 * if this is the first fix and the radius overlay does not have a
			 * point specified then pan the map, and zoom in to the users
			 * current location
			 */
			if (isFirstFix) {
				mProgress.setVisibility(View.INVISIBLE);
				if (mMap != null)
					if(mCircle.getCenter().equals(new LatLng(0, 0)))
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPoint, 14));

			}
		} else
			mProgress.setVisibility(View.VISIBLE);
	}

	/**
	 * Called when the GPS has a location to report
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onLocationChanged(final LatLng point, final int accuracy) {
		mPoint = point;
	}

	/**
	 * (non-Javadoc)
	 * 
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
			mRadiusTextView.setText(GeoUtils.distanceToString(Float.valueOf(progress) / 1000, true));
			mCircle.setRadius(progress);
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
	 */
	@Override
	public void onResume() {
		if (mMapEditToggle.isChecked())
			mGPS.enableLocationUpdates(this);
		setUpMapIfNeeded();
		super.onResume();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.RickBarrette.android.LocationRinger.SearchRequestedListener#onSearchRequested()
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

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
	 * installed) and the map has not already been instantiated.. This will ensure that we only ever
	 * call {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
	 * install/update the Google Play services APK on their device.
	 * <p>
	 * A user can return to this FragmentActivity after following the prompt and correctly
	 * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
	 * completely destroyed during this process (it is likely that it would only be stopped or
	 * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
	 * {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the camera.
	 * This should only be called once and when we are sure that {@link #mMap} is not null.
	 */
	private void setUpMap() {

		//Create the circle overlay and add it to the map
		final CircleOptions circle =  new CircleOptions();
		circle.strokeColor(Color.BLUE);
		circle.fillColor(Color.argb(100, 0, 0, 255));
		circle.center(new LatLng(0, 0));
		circle.radius(0);
		mCircle = mMap.addCircle(circle);

		final MarkerOptions marker = new MarkerOptions();
		marker.draggable(true);
		marker.position(mCircle.getCenter());
		marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		mMarker = mMap.addMarker(marker);

		mMap.setOnMarkerDragListener(this);

		enableMap(false);
	}

	/**
	 * Enables/Disables the zoom and scroll gestures of the map
	 * @param isEnabled
	 */
	private void enableMap(final boolean isEnabled){
		mMap.getUiSettings().setAllGesturesEnabled(isEnabled);
		mMap.getUiSettings().setZoomControlsEnabled(isEnabled);
	}

	/**
	 * Called when the Map is clicked
	 * @param point
	 */
	@Override
	public void onMapClick(LatLng point) {
		onLocationSelected(point);
	}

	/**
	 * Called when a location is selected in the search dialog
	 * @param point
	 */
	@Override
	public void onLocationSelected(LatLng point) {
		if (point != null) {
			Log.d(TAG, "onLocationSelected() " + point.toString());

			updateOverlay(point);

			if (mMap != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

			updateLocation(point);
		} else
			Log.d(TAG, "onLocationSelected() Location was null");
	}

	/**
	 * Updates the location used in the database and notifies maintainer
	 * @param location
	 */
	private void updateLocation(LatLng location) {
		if (mListener != null) {
			final ContentValues info = new ContentValues();
			final StringBuilder sb = new StringBuilder();
			sb.append(location.latitude).append(",").append(location.longitude);
			info.put(RingerDatabase.KEY_LOCATION, sb.toString());
			mListener.onInfoContentChanged(info);
		}
	}

	/**
	 * Updates the marker and the circle
	 * @param point
	 */
	private void updateOverlay(final LatLng point) {
		if (mCircle != null)
			mCircle.setCenter(point);

		if(mMarker != null)
			mMarker.setPosition(point);
	}

	/**
	 * Called at the start of a marker's drag cycle
	 * @param marker
	 */
	@Override
	public void onMarkerDragStart(Marker marker) {
		final LatLng location = marker.getPosition();
		updateOverlay(location);
		updateLocation(location);
	}

	/**
	 * Called when a marker is being dragged
	 * @param marker
	 */
	@Override
	public void onMarkerDrag(Marker marker) {
		final LatLng location = marker.getPosition();
		updateOverlay(location);
		updateLocation(location);
	}

	/**
	 * Called at the end of a marker's drag cycle
	 * @param marker
	 */
	@Override
	public void onMarkerDragEnd(Marker marker) {
		final LatLng location = marker.getPosition();
		updateOverlay(location);
		updateLocation(location);
	}
}