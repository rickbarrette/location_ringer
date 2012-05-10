/**
 * TitleFragmentAdapter.java
 * @date Aug 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.jakewharton.android.viewpagerindicator;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * This adaptor maintains a ViewPager title indicator.
 * @author ricky
 */
public class TitledFragmentAdapter extends FragmentAdapter implements TitleProvider {
	
	private String[] mTitles;
	
	/**
	 * Creates a new TitleFragmentAdapter
	 * @param fm
	 * @param fragments to be displayed
	 * @param titles for the fragments
	 * @author ricky barrette
	 */
	public TitledFragmentAdapter(FragmentManager fm, ArrayList<Fragment> fragments, String[] titles) {
		super(fm, fragments);
		this.mTitles = titles;
	}

	@Override
	public String getTitle(int position) {
		return this.mTitles[position];
	}
}