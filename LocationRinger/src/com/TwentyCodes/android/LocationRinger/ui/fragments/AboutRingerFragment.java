/**
 * AboutRingerFragment.java
 * @date Aug 8, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.Map.Entry;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * This fragment will used to allow the user to enter/edit ringer information
 * @author ricky
 */
@SuppressLint({ "ValidFragment", "ValidFragment" })
public class AboutRingerFragment extends Fragment implements OnCheckedChangeListener {

	private static final String TAG = "AboutRingerFragment";
	private ListeningEditText mRingerName;
	private ListeningEditText mRingerDescription;
	private ToggleButton mRingerEnabled;
	private final OnContentChangedListener mListener;
	private final ContentValues mInfo;
	private final ContentValues mRinger;
	
	public AboutRingerFragment(final ContentValues ringer, final ContentValues info, final OnContentChangedListener listener){
		super();
		this.mInfo = info;
		this.mRinger = ringer;
		this.mListener = listener;
	}
	
	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
		if(this.mListener != null){
			final ContentValues info = new ContentValues();
			info.put(RingerDatabase.KEY_IS_ENABLED, isChecked);
			this.mListener.onRingerContentChanged(info);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		final View view = inflater.inflate(R.layout.ringer_about_fragment, container, false);
		
		if(Debug.DEBUG){
			for(Entry<String,Object> item : this.mInfo.valueSet())
				Log.d(TAG, item.getKey() +" = "+ item.getValue());
					
			for(Entry<String,Object> item : this.mRinger.valueSet())
				Log.d(TAG, item.getKey() +" = "+ item.getValue());
		}
		
		/*
		 * ringer name
		 */
		this.mRingerName = (ListeningEditText) view.findViewById(R.id.ringer_name);
		if(this.mRinger.containsKey(RingerDatabase.KEY_RINGER_NAME))
			this.mRingerName.setText(this.mRinger.getAsString(RingerDatabase.KEY_RINGER_NAME));
		this.mRingerName.setKey(RingerDatabase.KEY_RINGER_NAME);
		this.mRingerName.setListener(this.mListener);
		
		/*
		 * ringer description
		 */
		this.mRingerDescription = (ListeningEditText) view.findViewById(R.id.ringer_description);
		if(this.mInfo.containsKey(RingerDatabase.KEY_RINGER_DESCRIPTION))
			this.mRingerDescription.setText(this.mInfo.getAsString(RingerDatabase.KEY_RINGER_DESCRIPTION));
		this.mRingerDescription.setKey(RingerDatabase.KEY_RINGER_DESCRIPTION);
		this.mRingerDescription.setListener(this.mListener);
		
		/*
		 * ringer enabled
		 */
		this.mRingerEnabled = (ToggleButton) view.findViewById(R.id.ringer_enabled);
		if(this.mRinger.containsKey(RingerDatabase.KEY_IS_ENABLED))
			this.mRingerEnabled.setChecked(this.mRinger.getAsBoolean(RingerDatabase.KEY_IS_ENABLED));
		this.mRingerEnabled.setOnCheckedChangeListener(this);
		
		return view;
	}
	
	/**
	 * This Edit text class is used in place of a standard edit text. 
	 * It will update the pass the updated information though a listener
	 * @author ricky barrette
	 */
	public static class ListeningEditText extends EditText{
		private String mKey;
		private OnContentChangedListener mListener;
		private final ContentValues mTemp;

		/**
		 * Creates a new ListeningEditText
		 * @param context
		 * @author ricky barrette
		 */
		public ListeningEditText(Context context) {
			super(context);
			this.mTemp = new ContentValues();
		}

		/**
		 * Creates a new ListeningEditText
		 * @param context
		 * @param attrs
		 * @author ricky barrette
		 */
		public ListeningEditText(Context context, AttributeSet attrs) {
			super(context, attrs);
			this.mTemp = new ContentValues();
		}

		/**
		 * Creates a new ListeningEditText
		 * @param context
		 * @param attrs
		 * @param defStyle
		 * @author ricky barrette
		 */
		public ListeningEditText(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			this.mTemp = new ContentValues();
		}
		
		/**
		 * Called when the edit text is drawn
		 * @author ricky barrette
		 */
		@Override
		public void onDraw(Canvas canvas){
			super.onDraw(canvas);
			if(mListener != null){
				mTemp.put(this.mKey, this.getText().toString());
				if(this.mKey.equals(RingerDatabase.KEY_RINGER_NAME))
					this.mListener.onRingerContentChanged(mTemp);
				else
					this.mListener.onInfoContentChanged(mTemp);
			}
		}
		
		/**
		 * Sets the key for this ListeningEditText
		 * @param key
		 * @ author ricky barrette
		 */
		public void setKey(String key){
			this.mKey = key;
		}
		
		/**
		 * Sets the listener of this ListeningEditText
		 * @param listener
		 * @ author ricky barrette
		 */
		public void setListener(OnContentChangedListener listener){
			this.mListener = listener;
		}
		
	}
}