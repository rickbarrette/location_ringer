/**
 * BaseFragmentListFragment.java
 * @date May 24, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package org.RickBarrette.android.LocationRinger.ui.fragments;

import java.util.ArrayList;
import java.util.Collections;

import org.RickBarrette.android.LocationRinger.Log;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * This fragment will be used to display a list of fragments
 * 
 * @author ricky barrette
 */
public abstract class BaseFragmentListFragment extends Fragment {

	private static final String TAG = "BaseFragmentListFragment";
	private final ArrayList<Fragment> mFragments;
	private final int mContainer;
	private final int mLayout;

	/**
	 * Creates a new Populated BaseFragmentListFragment
	 * 
	 * @author ricky barrette
	 */
	public BaseFragmentListFragment(final ArrayList<Fragment> fragments, final int layout, final int container) {
		super();
		mFragments = fragments;
		mLayout = layout;
		mContainer = container;
	}

	/**
	 * Creates a new Empty Base Fragment List
	 * 
	 * @param layout
	 * @param container
	 * @author ricky barrette
	 */
	public BaseFragmentListFragment(final int layout, final int container) {
		mLayout = layout;
		mContainer = container;
		mFragments = new ArrayList<Fragment>();
	}

	/**
	 * Adds the fragment to the list
	 * 
	 * @param fragment
	 * @author ricky barrette
	 */
	public void add(final Fragment fragment) {
		mFragments.add(fragment);
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(mContainer, fragment, fragment.getTag());
		transaction.commit();
	}

	/**
	 * Adds a collection ofs fragments to the list
	 * 
	 * @param fragments
	 * @author ricky barrette
	 */
	public void addAll(final ArrayList<Fragment> fragments) {
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		for (final Fragment f : fragments) {
			mFragments.add(f);
			transaction.add(mContainer, f, f.getTag());
		}
		transaction.commit();
	}

	/**
	 * Adds a collection ofs fragments to the list, but doesn't preform any
	 * transactions
	 * 
	 * @param fragment
	 * @author ricky barrette
	 */
	protected void addAllInit(final ArrayList<Fragment> fragments) {
		for (final Fragment f : fragments)
			mFragments.add(f);
	}

	/**
	 * Loads all the fragments
	 * 
	 * @author ricky barrette
	 */
	private void loadFragments() {
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		for (final Fragment fragment : mFragments)
			transaction.add(mContainer, fragment, fragment.getTag());
		transaction.commit();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@Override
	public void onActivityResult(final int arg0, final int arg1, final Intent arg2) {
		removeFragments();
		loadFragments();
		super.onActivityResult(arg0, arg1, arg2);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(final LayoutInflater inflator, final ViewGroup container, final Bundle bundle) {
		return inflator.inflate(mLayout, null);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		try {
			removeFragments();
		} catch (final IllegalStateException e) {
			e.printStackTrace();
			// do nothing
		}
		Collections.reverse(mFragments);
		super.onPause();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		Log.v(TAG, "onResume()");
		loadFragments();
		super.onResume();
	}

	/**
	 * Removes a fragment from the list
	 * 
	 * @param fragment
	 * @author ricky barrette
	 */
	public void remove(final Fragment fragment) {
		mFragments.remove(fragment);
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.remove(fragment);
		transaction.commit();
	}

	/**
	 * Removes all fragments from the the view
	 * 
	 * @throws IllegalStateException
	 * @author ricky barrette
	 */
	private void removeFragments() throws IllegalStateException {
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		for (final Fragment fragment : mFragments)
			transaction.remove(fragment);
		transaction.commit();
	}
}
