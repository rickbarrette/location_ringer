/**
 * ToggleButtonFragment.java
 * @date Aug 13, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.TwentyCodes.android.LocationRinger.FeatureRemovedListener;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;

/**
 * A simple fragment that displays a toggle button and a title label
 * 
 * @author ricky
 */
@SuppressLint("ValidFragment")
public class ToggleButtonFragment extends BaseFeatureFragment implements OnCheckedChangeListener {

	private final String mTitle;
	private final String mKey;
	private final ContentValues mInfo;
	private final OnContentChangedListener mChangedListener;

	/**
	 * Creates a new ToggleButtonFtagment
	 * 
	 * @author ricky barrette
	 */
	public ToggleButtonFragment(final int icon, final String title, final String key, final ContentValues info, final OnContentChangedListener changedListener,
			final FeatureRemovedListener removedListener, final int id) {
		super(id, R.layout.toggle_button_fragment, icon, removedListener);

		if (info == null)
			throw new NullPointerException();
		if (title == null)
			throw new NullPointerException();
		if (key == null)
			throw new NullPointerException();
		if (changedListener == null)
			throw new NullPointerException();

		mTitle = title;
		mKey = key;
		mInfo = info;
		mChangedListener = changedListener;
	}

	/**
	 * Called when the toggle button is clicked (non-Javadoc)
	 * 
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton,
	 *      boolean)
	 */
	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
		if (mChangedListener != null) {
			final ContentValues info = new ContentValues();
			info.put(mKey, isChecked);
			mChangedListener.onInfoContentChanged(info);
		}
	}

	/**
	 * Called when the fragment's view needs to be created (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final TextView t = (TextView) view.findViewById(R.id.title);
		t.setText(mTitle);

		final ToggleButton b = (ToggleButton) view.findViewById(R.id.toggle);
		if (mInfo.containsKey(mKey))
			b.setChecked(RingerDatabase.parseBoolean(mInfo.getAsString(mKey)));
		b.setOnCheckedChangeListener(this);
		return view;
	}
}