/**
 * RingtoneFragment.java
 * @date Aug 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.Map.Entry;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will be for ringtone settings
 * @author ricky
 */
public class RingtoneFragment extends Fragment implements OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener {
	
	private static final String TAG = "RingtoneFragment";
	private ToggleButton mRingtoneToggle;
	private String mRingtoneURI;
	private EditText mRingtone;
	private SeekBar mRingtonVolume;
	private Button mRingtoneButton;
	private int mStream;
	private int mType;
	private OnContentChangedListener mListener;
	private String mKeyEnabled;
	private String mKeyUri;
	private String mKeyRingtone;
	private String mKeyVolume;
	private ContentValues mInfo;
	
	public RingtoneFragment(ContentValues info, OnContentChangedListener listener, int stream){
		super();
		this.mListener = listener;
		this.mStream = stream;
		this.mInfo = info;
		
		switch(stream){
			case AudioManager.STREAM_RING:
				mKeyEnabled = RingerDatabase.KEY_RINGTONE_IS_SILENT;
				mKeyUri = RingerDatabase.KEY_RINGTONE_URI;
				mKeyRingtone = RingerDatabase.KEY_RINGTONE;
				mKeyVolume = RingerDatabase.KEY_RINGTONE_VOLUME;
				break;
			case AudioManager.STREAM_NOTIFICATION:
				mKeyEnabled = RingerDatabase.KEY_NOTIFICATION_IS_SILENT;
				mKeyUri = RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI;
				mKeyRingtone = RingerDatabase.KEY_NOTIFICATION_RINGTONE;
				mKeyVolume = RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME;
				break;
		}
	}

	/**
	 * starts the ringtone picker 
	 * @param ringtoneCode RingtoneManager.TYPE_?
	 * @param uri of current tone
	 * @author ricky barrette
	 */
	private void getRingtoneURI(int ringtoneCode, String uri){
        Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneCode);
        intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        if( uri == null)
			try {
				uri = RingtoneManager.getActualDefaultRingtoneUri(this.getActivity(), ringtoneCode).toString();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri == null ? null : Uri.parse(uri));
        startActivityForResult( intent, ringtoneCode);  
	}
	
	/**
	 * Called when the ringtone picker activity returns it's result
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Uri tone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if(tone == null){
				this.mRingtone.setText(R.string.silent);
			} else {
				Ringtone ringtone = RingtoneManager.getRingtone(this.getActivity(), Uri.parse(tone.toString()));
				this.mRingtone.setText(ringtone.getTitle(this.getActivity()));
			}
			
			if(this.mListener != null){
				ContentValues info = new ContentValues();
				info.put(this.mKeyRingtone, this.mRingtone.getText().toString());
				info.put(this.mKeyUri, tone != null ? tone.toString() : null);
				this.mListener.onInfoContentChanged(info);
			}
		}
	}
	
	/**
	 * Called when a toggle button's state is changed
	 * @author ricky barrette
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		this.mRingtone.setEnabled(!isChecked);
		this.mRingtoneButton.setEnabled(!isChecked);
		this.mRingtonVolume.setEnabled(!isChecked);
		if(this.mListener != null){
			ContentValues info = new ContentValues();
			info.put(this.mKeyEnabled, isChecked);
			this.mListener.onInfoContentChanged(info);
		}
	}
	
	/**
	 * Called when a view is clicked
	 * @author ricky barrette
	 */
	@Override
	public void onClick(View v) {
		getRingtoneURI(this.mType, mRingtoneURI);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.ringtone_fragment, container, false);
		AudioManager mAudioManager = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);
		
		if(Debug.DEBUG)
			for(Entry<String,Object> item : this.mInfo.valueSet())
				Log.d(TAG, item.getKey() +" = "+ item.getValue());
		
		/*
		 * initialize the views
		 */
		TextView label = (TextView) view.findViewById(R.id.label);
		this.mRingtone = (EditText) view.findViewById(R.id.ringtone);
		this.mRingtoneToggle = (ToggleButton) view.findViewById(R.id.ringtone_silent_toggle);
		this.mRingtonVolume = (SeekBar) view.findViewById(R.id.ringtone_volume);
		this.mRingtoneButton = (Button) view.findViewById(R.id.ringtone_button);
		
		this.mRingtoneButton.setOnClickListener(this);
		this.mRingtonVolume.setMax(mAudioManager.getStreamMaxVolume(mStream));
		
		switch(this.mStream){
			case AudioManager.STREAM_RING:
				label.setText(R.string.ringtone);
				mType = RingtoneManager.TYPE_RINGTONE;
				break;
			case AudioManager.STREAM_NOTIFICATION:
				label.setText(R.string.notification_ringtone);
				mType = RingtoneManager.TYPE_NOTIFICATION;
				break;
		}
		
		/*
		 * ringtone & uri
		 */
		if(this.mInfo.containsKey(this.mKeyUri) && this.mInfo.containsKey(this.mKeyRingtone)){
			this.mRingtone.setText(this.mInfo.getAsString(this.mKeyRingtone));
			this.mRingtoneURI = this.mInfo.getAsString(this.mKeyUri);
		} else
			try {
				this.mRingtoneURI = RingtoneManager.getActualDefaultRingtoneUri(this.getActivity(), mType).toString();
				this.mRingtone.setText(RingtoneManager.getRingtone(this.getActivity(), Uri.parse(mRingtoneURI)).getTitle(this.getActivity()));
			} catch (NullPointerException e) {
				e.printStackTrace();
				this.mRingtoneToggle.setChecked(true);
			}
			
		/*
		 * volume
		 */
		if(this.mInfo.containsKey(this.mKeyVolume))
			this.mRingtonVolume.setProgress(Integer.parseInt(this.mInfo.getAsString(this.mKeyVolume)));
		else
			this.mRingtonVolume.setProgress(mAudioManager.getStreamVolume(mStream));
		
		/*
		 * silent toggle
		 */
		if(this.mInfo.containsKey(this.mKeyEnabled))
			this.mRingtoneToggle.setChecked(!this.mInfo.getAsBoolean(this.mKeyEnabled));
		else
			this.mRingtoneToggle.setChecked(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ? true : false);

		/*
		 * disable views if need be
		 */
		this.mRingtoneToggle.setOnCheckedChangeListener(this);
		this.mRingtone.setEnabled(! mRingtoneToggle.isChecked());
		this.mRingtonVolume.setEnabled(! mRingtoneToggle.isChecked());
		this.mRingtonVolume.setOnSeekBarChangeListener(this);
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser)
			if(this.mListener != null){
				ContentValues info = new ContentValues();
				info.put(this.mKeyVolume, progress);
				this.mListener.onInfoContentChanged(info);
			}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
}