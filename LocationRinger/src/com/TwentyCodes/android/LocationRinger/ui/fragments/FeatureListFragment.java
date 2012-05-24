/**
 * FeatureListFragment.java
 * @date Aug 7, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will be used to display a list of fragments
 * TODO 
 * + create button bar that had a plus button and a hint + add/remove features
 * 
 * @author ricky
 */
public class FeatureListFragment extends Fragment implements OnClickListener, android.content.DialogInterface.OnClickListener {

	private static final String TAG = "FeatureListFragment";
	private final ArrayList<Fragment> mFragments;
	private final ContentValues mInfo;
	private final OnContentChangedListener mListener;
	private final ArrayList<Integer> mAdded;

	/**
	 * Creates a new FeatureListFragment
	 * @param info
	 * @param listener
	 * @param fragments
	 * @author ricky barrette
	 */
	public FeatureListFragment(ContentValues info, OnContentChangedListener listener, ArrayList<Fragment> fragments, ArrayList<Integer> added) {
		super();
		mFragments = fragments;
		mInfo = info;
		mListener = listener;
		mAdded = added;
	}

	/**
	 * Adds the fragment to the list
	 * @param fragment
	 * @author ricky barrette
	 */
	public void add(final Fragment fragment){
		this.mFragments.add(fragment);
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		transaction.add(R.id.fragment_list_contianer, fragment, fragment.getTag());
		transaction.commit();
	}

	/**
	 * Loads all the fragments
	 * @author ricky barrette
	 */
	private void loadFragments() {
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		for(Fragment fragment : this.mFragments)
			transaction.add(R.id.fragment_list_contianer, fragment, fragment.getTag());
		transaction.commit();
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int arg0, int arg1, Intent arg2) {
		removeFragments();
		loadFragments();
		super.onActivityResult(arg0, arg1, arg2);
	}

	/**
	 * Called when an item is picked from the add featue list
	 * (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		Fragment f = null;
		switch(which){
			case 0:
				f= new RingtoneFragment(this.mInfo, this.mListener, AudioManager.STREAM_RING);
				mAdded.add(0);
				break;
			case 1:
				f = new RingtoneFragment(this.mInfo, this.mListener, AudioManager.STREAM_NOTIFICATION);
				mAdded.add(1);
				break;
			case 2:
				f = new VolumeFragment(this.mInfo, this.getActivity(), this.mListener, AudioManager.STREAM_ALARM);
				mAdded.add(2);
				break;
			case 3:
				f = new VolumeFragment(this.mInfo, this.getActivity(), this.mListener, AudioManager.STREAM_MUSIC);
				mAdded.add(3);
				break;
			case 4:
				f = new ToggleButtonFragment(this.getString(R.string.bluetooth), RingerDatabase.KEY_BT, this.mInfo, this.mListener);
				mAdded.add(4);
				break;
			case 5:
				f = new ToggleButtonFragment(this.getString(R.string.wifi), RingerDatabase.KEY_WIFI, this.mInfo, this.mListener);
				mAdded.add(5);
				break;
//			case 6:
//				f = 
//				break;
		}
		
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
	 * (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle bundle) {
		final View v = inflator.inflate(R.layout.fragment_list_contianer, null);
		v.findViewById(R.id.add_feature_button).setOnClickListener(this);
		return v;
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		try{
			removeFragments();
		} catch(IllegalStateException e){
			e.printStackTrace();
			//do nothing
		}
		Collections.reverse(this.mFragments);
		super.onPause();
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		if (Debug.DEBUG)
			Log.v(TAG, "onResume()");
		loadFragments();
		super.onResume();
	}

	/**
	 * Removes a fragment from the list
	 * @param fragment
	 * @author ricky barrette
	 */
	public void remove(final Fragment fragment){
		this.mFragments.remove(fragment);
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		transaction.remove(fragment);
		transaction.commit();
	}

	/**
	 * Removes all fragments from the the view
	 * @throws IllegalStateException
	 * @author ricky barrette
	 */
	private void removeFragments() throws IllegalStateException {
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		for(Fragment fragment : this.mFragments){
            transaction.remove(fragment);
		}
		transaction.commit();
	}
}