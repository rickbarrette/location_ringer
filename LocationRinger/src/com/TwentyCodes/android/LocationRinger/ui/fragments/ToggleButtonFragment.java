/**
 * ToggleButtonFragment.java
 * @date Aug 13, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.TwentyCodes.android.LocationRinger.FeatureRemovedListener;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;

/**
 * A simple fragment that displays a toggle button and a title label
 * @author ricky
 */
public class ToggleButtonFragment extends IdFragment implements OnCheckedChangeListener, OnClickListener {

	private final String mTitle;
	private final String mKey;
	private final ContentValues mInfo;
	private final OnContentChangedListener mChangedListener;
	private final int mIcon;
	private final FeatureRemovedListener mRemovedListener;

	/**
	 * Creates a new ToggleButtonFtagment
	 * @author ricky barrette
	 */
	public ToggleButtonFragment(int icon, String title, String key, ContentValues info, OnContentChangedListener changedListener, FeatureRemovedListener removedListener, int id) {
		super(id);
		
		if ( info == null )
			throw new NullPointerException();
		if (title == null )
			throw new NullPointerException();
		if ( key == null )
			throw new NullPointerException();
		if ( changedListener == null )
			throw new NullPointerException();
		if ( removedListener == null )
			throw new NullPointerException();
		
		this.mTitle = title;
		this.mKey = key;
		this.mInfo = info;
		this.mChangedListener = changedListener;
		this.mIcon = icon;
		this.mRemovedListener = removedListener;
	}

	/**
	 * Called when the fragment's view needs to be created
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
		final View view =  inflater.inflate(R.layout.toggle_button_fragment, container, false);
		final TextView t = (TextView) view.findViewById(R.id.title);
		t.setText(this.mTitle);
		
		final ImageView icon = (ImageView) view.findViewById(R.id.icon);
		icon.setImageDrawable(this.getActivity().getResources().getDrawable(mIcon));
		
		view.findViewById(R.id.close).setOnClickListener(this);
		
		final ToggleButton b = (ToggleButton) view.findViewById(R.id.toggle);
		if(this.mInfo.containsKey(this.mKey))
			b.setChecked(RingerDatabase.parseBoolean(this.mInfo.getAsString(this.mKey)));
		b.setOnCheckedChangeListener(this);
		return view;
	}

	/**
	 * Called when the toggle button is clicked
	 * (non-Javadoc)
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(this.mChangedListener != null){
			ContentValues info = new ContentValues();
			info.put(this.mKey, isChecked);
			this.mChangedListener.onInfoContentChanged(info);
		}
	}

	/**
	 * Called when the user clicks the remove button
	 * @param v
	 * @author ricky barrette
	 */
	@Override
	public void onClick(View v) {
		if(this.mRemovedListener != null)
			this.mRemovedListener.onFeatureRemoved(this);
	}
}