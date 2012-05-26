/**
 * ToggleButtonFragment.java
 * @date Aug 13, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * A simple fragment that displays a toggle button and a title label
 * @author ricky
 */
public class ToggleButtonFragment extends Fragment implements OnCheckedChangeListener {

	private String mTitle;
	private String mKey;
	private ContentValues mInfo;
	private OnContentChangedListener mListener;

	/**
	 * Creates a new ToggleButtonFtagment
	 * @author ricky barrette
	 */
	public ToggleButtonFragment(String title, String key, ContentValues info, OnContentChangedListener listener) {
		super();
		this.mTitle = title;
		this.mKey = key;
		this.mInfo = info;
		this.mListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
		View view =  inflater.inflate(R.layout.toggle_button_fragment, container, false);
		TextView t = (TextView) view.findViewById(R.id.title);
		t.setText(this.mTitle);
		
		final ImageView icon = (ImageView) view.findViewById(R.id.icon);
		icon.setImageDrawable(this.getActivity().getResources().getDrawable(android.R.drawable.ic_lock_silent_mode_off));
		
		ToggleButton b = (ToggleButton) view.findViewById(R.id.toggle);
		if(this.mInfo.containsKey(this.mKey))
			b.setChecked(RingerDatabase.parseBoolean(this.mInfo.getAsString(this.mKey)));
		b.setOnCheckedChangeListener(this);
		return view;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(this.mListener != null){
			ContentValues info = new ContentValues();
			info.put(this.mKey, isChecked);
			this.mListener.onInfoContentChanged(info);
		}
	}

}