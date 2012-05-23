/**
 * FeatureListFragment.java
 * @date Aug 7, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.ArrayList;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will be used to display a list of fragments
 * TODO 
 * + create button bar that had a plus button and a hint + add/remove features
 * 
 * @author ricky
 */
public class FeatureListFragment extends Fragment {

	private static final String TAG = "FeatureListFragment";
	private final ArrayList<Fragment> mFragments;

	/**
	 * Creates a new FeatureListFragment
	 * @param info
	 * @param listener
	 * @param fragments
	 * @author ricky barrette
	 */
	public FeatureListFragment(ContentValues info, OnContentChangedListener listener, ArrayList<Fragment> fragments) {
		super();
		mFragments = fragments;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle bundle) {
		return inflator.inflate(R.layout.fragment_list_contianer, null);
	}

	@Override
	public void onResume() {
		if (Debug.DEBUG)
			Log.v(TAG, "onResume()");
		loadFragments();
		super.onResume();
	}

	/**
	 * Loads all the fragments
	 * @author ricky barrette
	 */
	private void loadFragments() {
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		for(Fragment fragment : this.mFragments){
            transaction.add(R.id.fragment_list_contianer, fragment, fragment.getTag());
		}
		transaction.commit();
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
}