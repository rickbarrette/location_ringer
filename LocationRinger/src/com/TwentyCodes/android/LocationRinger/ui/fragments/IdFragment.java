/**
 * IdFragment.java
 * @date May 26, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import android.support.v4.app.Fragment;

/**
 * This is a simple extention of a fragment that will allow for storage of an id
 * @author ricky barrette
 */
public class IdFragment extends Fragment {

	private final int mId;

	public IdFragment(int id){
		super();
		mId = id;
	}

	/**
	 * @return the id of this fragment
	 */
	public int getFragmentId() {
		return mId;
	}
}