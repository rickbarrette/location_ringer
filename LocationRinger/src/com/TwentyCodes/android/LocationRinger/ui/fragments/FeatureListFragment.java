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
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will be used to display a list of fragments
 * 
 * TODO + create button bar that had a plus button and a hint + add/remove
 * features
 * 
 * @author ricky
 */
public class FeatureListFragment extends ListFragment {

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container,
			Bundle bundle) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflator, container, bundle);
	}

	private static final String TAG = "FeatureListFragment";
	private static final int DELETE_ID = 0;
	private ArrayList<Fragment> mFeatures;

	// private OnContentChangedListener mListener;
	// private ContentValues mInfo;
	// private int mIndex;

	public FeatureListFragment(ContentValues info,
			OnContentChangedListener listener, ArrayList<Fragment> fragments) {
		super();
		this.mFeatures = fragments;
		// this.mInfo = info;
		// this.mListener = listener;
	}

	@Override
	public void onResume() {
		this.setListAdapter(new FragmentListAdaptor(this, mFeatures));
		this.getListView().setOnCreateContextMenuListener(this);
		if (Debug.DEBUG)
			Log.v(TAG, "onResume()");
		super.onResume();
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.delete).setIcon(
				android.R.drawable.ic_menu_delete);
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			Toast.makeText(this.getActivity(), "deleted! (note really)",
					Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onContextItemSelected(item);
	}
}