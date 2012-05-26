/**
 * BaseFragmentListFragment.java
 * @date May 24, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will be used to display a list of fragments
 * @author ricky barrette
 */
public abstract class BaseFragmentListFragment extends Fragment {

	private static final String TAG = "BaseFragmentListFragment";
	private final ArrayList<Fragment> mFragments;
	private final int mContainer;
	private final int mLayout;

	/**
	 * Creates a new Populated BaseFragmentListFragment
	 * @author ricky barrette
	 */
	public BaseFragmentListFragment(ArrayList<Fragment> fragments, int layout, int container) {
		super();
		mFragments = fragments;
		mLayout = layout;
		mContainer = container;
	}

	/**
	 * Creates a new Empty Base Fragment List
	 * @param layout
	 * @param container
	 * @author ricky barrette
	 */
	public BaseFragmentListFragment(int layout, int container) {
		mLayout = layout;
		mContainer = container;
		mFragments = new ArrayList<Fragment>();
	}

	/**
	 * Adds the fragment to the list
	 * @param fragment
	 * @author ricky barrette
	 */
	public void add(final Fragment fragment){
		this.mFragments.add(fragment);
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		transaction.add(mContainer, fragment, fragment.getTag());
		transaction.commit();
	}
	
	/**
	 * Adds a collection ofs fragments to the list
	 * @param fragments
	 * @author ricky barrette
	 */
	public void addAll(final ArrayList<Fragment> fragments){
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		for(Fragment f : fragments){
			this.mFragments.add(f);
			transaction.add(mContainer, f, f.getTag());
		}
		transaction.commit();
	}
	
	/**
	 * Adds a collection ofs fragments to the list, but doesn't preform any transactions
	 * @param fragment
	 * @author ricky barrette
	 */
	protected void addAllInit(final ArrayList<Fragment> fragments){
		for(Fragment f : fragments){
			this.mFragments.add(f);
		}
	}
	
	/**
	 * Loads all the fragments
	 * @author ricky barrette
	 */
	private void loadFragments() {
		final FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
		for(Fragment fragment : this.mFragments)
			transaction.add(mContainer, fragment, fragment.getTag());
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
	 * (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle bundle) {
		return inflator.inflate(mLayout, null);
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
