/**
 * FragmentAdapter.java
 * @date Aug 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.jakewharton.android.viewpagerindicator;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * This adaptor maintains the How and What fragments
 * 
 * @author ricky
 */
class FragmentAdapter extends FragmentPagerAdapter {

	private final ArrayList<Fragment> mFragments;

	/**
	 * Creates a new FragmentAdaptor
	 * 
	 * @param fm
	 * @param fragments
	 *            to be displayed
	 * @author ricky barrette
	 */
	public FragmentAdapter(final FragmentManager fm, final ArrayList<Fragment> fragments) {
		super(fm);
		mFragments = fragments;
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	@Override
	public Fragment getItem(final int position) {
		return mFragments.get(position);
	}

}