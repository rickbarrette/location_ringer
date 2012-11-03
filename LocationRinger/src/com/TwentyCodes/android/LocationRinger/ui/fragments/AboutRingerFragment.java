/**
 * AboutRingerFragment.java
 * @date Aug 8, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.TwentyCodes.android.LocationRinger.Log;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;

/**
 * This fragment will used to allow the user to enter/edit ringer information
 * 
 * @author ricky
 */
@SuppressLint({ "ValidFragment", "ValidFragment" })
public class AboutRingerFragment extends Fragment implements OnCheckedChangeListener {

	/**
	 * This Edit text class is used in place of a standard edit text. It will
	 * update the pass the updated information though a listener
	 * 
	 * @author ricky barrette
	 */
	public static class ListeningEditText extends EditText {
		private String mKey;
		private OnContentChangedListener mListener;
		private final ContentValues mTemp;

		/**
		 * Creates a new ListeningEditText
		 * 
		 * @param context
		 * @author ricky barrette
		 */
		public ListeningEditText(final Context context) {
			super(context);
			mTemp = new ContentValues();
		}

		/**
		 * Creates a new ListeningEditText
		 * 
		 * @param context
		 * @param attrs
		 * @author ricky barrette
		 */
		public ListeningEditText(final Context context, final AttributeSet attrs) {
			super(context, attrs);
			mTemp = new ContentValues();
		}

		/**
		 * Creates a new ListeningEditText
		 * 
		 * @param context
		 * @param attrs
		 * @param defStyle
		 * @author ricky barrette
		 */
		public ListeningEditText(final Context context, final AttributeSet attrs, final int defStyle) {
			super(context, attrs, defStyle);
			mTemp = new ContentValues();
		}

		/**
		 * Called when the edit text is drawn
		 * 
		 * @author ricky barrette
		 */
		@Override
		public void onDraw(final Canvas canvas) {
			super.onDraw(canvas);
			if (mListener != null) {
				mTemp.put(mKey, this.getText().toString());
				if (mKey.equals(RingerDatabase.KEY_RINGER_NAME))
					mListener.onRingerContentChanged(mTemp);
				else
					mListener.onInfoContentChanged(mTemp);
			}
		}

		/**
		 * Sets the key for this ListeningEditText
		 * 
		 * @param key
		 *            @ author ricky barrette
		 */
		public void setKey(final String key) {
			mKey = key;
		}

		/**
		 * Sets the listener of this ListeningEditText
		 * 
		 * @param listener
		 *            @ author ricky barrette
		 */
		public void setListener(final OnContentChangedListener listener) {
			mListener = listener;
		}

	}

	private static final String TAG = "AboutRingerFragment";
	private ListeningEditText mRingerName;
	private ListeningEditText mRingerDescription;
	private ToggleButton mRingerEnabled;
	private final OnContentChangedListener mListener;
	private final ContentValues mInfo;

	private final ContentValues mRinger;

	public AboutRingerFragment(final ContentValues ringer, final ContentValues info, final OnContentChangedListener listener) {
		super();
		mInfo = info;
		mRinger = ringer;
		mListener = listener;
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
		if (mListener != null) {
			final ContentValues info = new ContentValues();
			info.put(RingerDatabase.KEY_IS_ENABLED, isChecked);
			mListener.onRingerContentChanged(info);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		final View view = inflater.inflate(R.layout.ringer_about_fragment, container, false);

		for (final Entry<String, Object> item : mInfo.valueSet())
			Log.d(TAG, item.getKey() + " = " + item.getValue());

		for (final Entry<String, Object> item : mRinger.valueSet())
			Log.d(TAG, item.getKey() + " = " + item.getValue());

		/*
		 * ringer name
		 */
		mRingerName = (ListeningEditText) view.findViewById(R.id.ringer_name);
		if (mRinger.containsKey(RingerDatabase.KEY_RINGER_NAME))
			mRingerName.setText(mRinger.getAsString(RingerDatabase.KEY_RINGER_NAME));
		mRingerName.setKey(RingerDatabase.KEY_RINGER_NAME);
		mRingerName.setListener(mListener);

		/*
		 * ringer description
		 */
		mRingerDescription = (ListeningEditText) view.findViewById(R.id.ringer_description);
		if (mInfo.containsKey(RingerDatabase.KEY_RINGER_DESCRIPTION))
			mRingerDescription.setText(mInfo.getAsString(RingerDatabase.KEY_RINGER_DESCRIPTION));
		mRingerDescription.setKey(RingerDatabase.KEY_RINGER_DESCRIPTION);
		mRingerDescription.setListener(mListener);

		/*
		 * ringer enabled
		 */
		mRingerEnabled = (ToggleButton) view.findViewById(R.id.ringer_enabled);
		if (mRinger.containsKey(RingerDatabase.KEY_IS_ENABLED))
			mRingerEnabled.setChecked(mRinger.getAsBoolean(RingerDatabase.KEY_IS_ENABLED));
		mRingerEnabled.setOnCheckedChangeListener(this);
		onCheckedChanged(mRingerEnabled, mRingerEnabled.isChecked());

		return view;
	}
}