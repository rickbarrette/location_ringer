/**
 * FeatureListFragment.java
 * @date Aug 7, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.TwentyCodes.android.LocationRinger.FeatureRemovedListener;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;

/**
 * This fragment will be used to display a list of features
 * @author ricky
 */
public class FeatureListFragment extends BaseFragmentListFragment implements OnClickListener, android.content.DialogInterface.OnClickListener, FeatureRemovedListener {
	
	private static final int KEY_ADDED_RINGTONE = 0;
	private static final int KEY_ADDED_NOTIFICATIONTONE = 1;
	private static final int KEY_ADDED_ALARM_VOLUME = 2;
	private static final int KEY_ADDED_MUSIC_VOLUME = 3;
	private static final int KEY_ADDED_BT = 4;
	private static final int KEY_ADDED_WIFI = 5;
	private final ContentValues mInfo;
	private final OnContentChangedListener mListener;
	private final ArrayList<Integer> mAdded;

	/**
	 * Creates a new populated FeatureListFragment
	 * @param info
	 * @param listener
	 * @param fragments
	 * @author ricky barrette
	 */
	public FeatureListFragment(ContentValues info, OnContentChangedListener listener, ArrayList<Fragment> fragments, ArrayList<Integer> added) {
		super(R.layout.fragment_list_contianer, R.id.fragment_list_contianer);
		
		if ( info == null )
			throw new NullPointerException();
		if ( listener == null )
			throw new NullPointerException();
		if ( fragments == null )
			throw new NullPointerException();
		if ( added == null )
			throw new NullPointerException();
		
		mInfo = info;
		mListener = listener;
		mAdded = added;
	}
	
	/**
	 * Creates a new empty feature list fragment
	 * @param info
	 * @param listener
	 * @author ricky barrette
	 */
	public FeatureListFragment(ContentValues info, OnContentChangedListener listener){
		this(info, listener, new ArrayList<Fragment>(), new ArrayList<Integer>());
	}

	/**
	 * Initializes a feature fragment
	 * @param fragmentCode
	 * @return
	 * @author ricky barrette
	 */
	public Fragment initFeatureFragment(int fragmentCode){
		Fragment f = null;
		switch(fragmentCode){
			case KEY_ADDED_RINGTONE:
				f= new RingtoneFragment(this.mInfo, this.mListener, this, AudioManager.STREAM_RING, KEY_ADDED_RINGTONE);
				mAdded.add(KEY_ADDED_RINGTONE);
				break;
			case KEY_ADDED_NOTIFICATIONTONE:
				f = new RingtoneFragment(this.mInfo, this.mListener, this, AudioManager.STREAM_NOTIFICATION, KEY_ADDED_NOTIFICATIONTONE);
				mAdded.add(KEY_ADDED_NOTIFICATIONTONE);
				break;
			case KEY_ADDED_ALARM_VOLUME:
				f = new VolumeFragment(this.mInfo, this.getActivity(), this.mListener, this, AudioManager.STREAM_ALARM, KEY_ADDED_ALARM_VOLUME);
				mAdded.add(KEY_ADDED_ALARM_VOLUME);
				break;
			case KEY_ADDED_MUSIC_VOLUME:
				f = new VolumeFragment(this.mInfo, this.getActivity(), this.mListener, this, AudioManager.STREAM_MUSIC, KEY_ADDED_MUSIC_VOLUME);
				mAdded.add(KEY_ADDED_MUSIC_VOLUME);
				break;
			case KEY_ADDED_BT:
				f = new ToggleButtonFragment(android.R.drawable.stat_sys_data_bluetooth, this.getString(R.string.bluetooth), RingerDatabase.KEY_BT, this.mInfo, this.mListener, this, KEY_ADDED_BT);
				mAdded.add(KEY_ADDED_BT);
				break;
			case KEY_ADDED_WIFI:
				f = new ToggleButtonFragment(android.R.drawable.stat_sys_data_bluetooth, this.getString(R.string.wifi), RingerDatabase.KEY_WIFI, this.mInfo, this.mListener, this, KEY_ADDED_WIFI);
				mAdded.add(KEY_ADDED_WIFI);
				break;
				
		}
		return f;
	}

	/**
	 * Initializes feature fragments based upon current records 
	 * @author ricky barrette
	 */
	private ArrayList<Fragment> initList() {
		ArrayList<Fragment> what = new ArrayList<Fragment>();
		
		if(this.mInfo.containsKey(RingerDatabase.KEY_RINGTONE_IS_SILENT) || this.mInfo.containsKey(RingerDatabase.KEY_RINGTONE_VOLUME)){
			what.add(initFeatureFragment(KEY_ADDED_RINGTONE));
		}
		
		if(this.mInfo.containsKey(RingerDatabase.KEY_NOTIFICATION_IS_SILENT) || this.mInfo.containsKey(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME)){
			what.add(initFeatureFragment(KEY_ADDED_NOTIFICATIONTONE));
		}
		
		if(this.mInfo.containsKey(RingerDatabase.KEY_ALARM_VOLUME)){
			what.add(initFeatureFragment(KEY_ADDED_ALARM_VOLUME));
		}
		
		if(this.mInfo.containsKey(RingerDatabase.KEY_MUSIC_VOLUME)){
			what.add(initFeatureFragment(KEY_ADDED_MUSIC_VOLUME));
		}
		
		if(this.mInfo.containsKey(RingerDatabase.KEY_BT)){
			what.add(initFeatureFragment(KEY_ADDED_BT));
		}
		
		if(this.mInfo.containsKey(RingerDatabase.KEY_WIFI)){
			what.add(initFeatureFragment(KEY_ADDED_WIFI));
		}
		return what;
	}
	
	/**
	 * Called when an item is picked from the add featue list
	 * (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		final Fragment f = initFeatureFragment(which);
		if(f != null)
			add(f);
	}

	/**
	 * Called when the add feature button is clicked
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		new AlertDialog.Builder(this.getActivity())
			.setTitle(R.string.add_feature)
			.setAdapter(
					new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, this.getResources().getStringArray(R.array.features)){
						
						/**
						 * we override this, because we want to filter which items are enabled
						 * (non-Javadoc)
						 * @see android.widget.BaseAdapter#areAllItemsEnabled()
						 */
						@Override
						public boolean areAllItemsEnabled(){
							return false;
						}
						
						/**
						 * here we want to grey out disabled items in the list
						 * (non-Javadoc)
						 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
						 */
						@Override
						public View getView(int position, View convertView, ViewGroup parent){
							final View v = super.getView(position, convertView, parent);
							v.setEnabled(isEnabled(position));
							
							if(Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 11){
								final TextView t = (TextView) v.findViewById(android.R.id.text1);
								t.setTextColor(isEnabled(position) ? Color.BLACK : Color.GRAY);
							}
									
							return v;
						}
						
						/**
						 * here we can notify the adaptor if an item should be enabled or not
						 * (non-Javadoc)
						 * @see android.widget.BaseAdapter#isEnabled(int)
						 */
						@Override
						public boolean isEnabled(int position){
							return ! mAdded.contains(position);
						}
			}, this)
			.show();
	}

	/**
	 * Called when the activity is first created
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle arg0) {
		addAllInit(initList());
		super.onCreate(arg0);
	}

	/**
	 * Called when the view needs to be created
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.LocationRinger.ui.fragments.BaseFragmentListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle bundle) {
		final View v =  super.onCreateView(inflator, container, bundle);
		v.findViewById(R.id.add_feature_button).setOnClickListener(this);
		return v;
	}

	/**
	 * Called when a fragment needs to be removed
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.LocationRinger.FeatureRemovedListener#onFeatureRemoved(android.support.v4.app.Fragment)
	 */
	@Override
	public void onFeatureRemoved(Fragment f) {
		this.remove(f);
		
		if(f instanceof BaseFeatureFragment){
			final int id =  ((BaseFeatureFragment) f).getFragmentId();
			mAdded.remove(Integer.valueOf(id));
			
			/*
			 * we need to notify our parent activity that the feature have been removed.
			 */
			switch(id){
				case KEY_ADDED_RINGTONE:
					this.mListener.onInfoContentRemoved(RingerDatabase.KEY_RINGTONE_URI, RingerDatabase.KEY_RINGTONE_IS_SILENT, RingerDatabase.KEY_RINGTONE_VOLUME);
					break;
				case KEY_ADDED_NOTIFICATIONTONE:
					this.mListener.onInfoContentRemoved(RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI, RingerDatabase.KEY_NOTIFICATION_IS_SILENT, RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME);
					break;
				case KEY_ADDED_ALARM_VOLUME:
					this.mListener.onInfoContentRemoved(RingerDatabase.KEY_ALARM_VOLUME);
					break;
				case KEY_ADDED_MUSIC_VOLUME:
					this.mListener.onInfoContentRemoved(RingerDatabase.KEY_MUSIC_VOLUME);
					break;
				case KEY_ADDED_BT:
					this.mListener.onInfoContentRemoved(RingerDatabase.KEY_BT);
					break;
				case KEY_ADDED_WIFI:
					this.mListener.onInfoContentRemoved(RingerDatabase.KEY_WIFI);
					break;
			}
		}
	}
}