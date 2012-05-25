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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will be for ringtone settings
 * @author ricky
 */
public class RingtoneFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener {
	
	private static final String TAG = "RingtoneFragment";
	private final int mStream;
	private final int mType;
	private final OnContentChangedListener mListener;
	private final String mKeyEnabled;
	private final String mKeyUri;
	private final String mKeyRingtone;
	private final String mKeyVolume;
	private final ContentValues mInfo;
	private final int mLabel;
	private EditText mRingtone;
	private String mRingtoneURI;
	private SeekBar mVolume;
	
	public RingtoneFragment(ContentValues info, OnContentChangedListener listener, int stream){
		super();
		this.mListener = listener;
		this.mStream = stream;
		this.mInfo = info;
		
		switch(stream){
			case AudioManager.STREAM_NOTIFICATION:
				mKeyEnabled = RingerDatabase.KEY_NOTIFICATION_IS_SILENT;
				mKeyUri = RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI;
				mKeyRingtone = RingerDatabase.KEY_NOTIFICATION_RINGTONE;
				mKeyVolume = RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME;
				mLabel = R.string.notification_ringtone;
				mType = RingtoneManager.TYPE_NOTIFICATION;
				break;
				
			case AudioManager.STREAM_RING:
			default:
				mKeyEnabled = RingerDatabase.KEY_RINGTONE_IS_SILENT;
				mKeyUri = RingerDatabase.KEY_RINGTONE_URI;
				mKeyRingtone = RingerDatabase.KEY_RINGTONE;
				mKeyVolume = RingerDatabase.KEY_RINGTONE_VOLUME;
				mLabel = R.string.ringtone;
				mType = RingtoneManager.TYPE_RINGTONE;
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
        final Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneCode);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
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
	 * Notifys the listener that the ringtone has changedRingtoneManager.getActualDefaultRingtoneUri(this.getActivity(), mType)
	 * @param tone
	 * @author ricky barrette
	 */
	private void notifyRingtoneChanged(Uri tone) {
		if(this.mListener != null){
			ContentValues info = new ContentValues();			
			info.put(this.mKeyRingtone, tone == null 
					? getString(R.string.silent) : RingtoneManager.getRingtone(this.getActivity(), Uri.parse(tone.toString())).getTitle(this.getActivity()));
			info.put(this.mKeyUri, tone != null ? tone.toString() : null);
			info.put(mKeyEnabled, tone == null);
			this.mListener.onInfoContentChanged(info);
		}
	}
	
	/**
	 * Notifys the listener that the volume has changed
	 * @param progress
	 * @author ricky barrette
	 */
	private void notifyVolumeChanged(int progress) {
		if(this.mListener != null){
			final ContentValues info = new ContentValues();
			info.put(this.mKeyVolume, progress);
			this.mListener.onInfoContentChanged(info);
		}
	}

	/**
	 * Called when the ringtone picker activity returns it's result
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			final Uri tone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if(tone == null){
				this.mRingtone.setText(R.string.silent);
				mVolume.setEnabled(false);
				mVolume.setProgress(0);
			} else {
				mVolume.setEnabled(true);
				Ringtone ringtone = RingtoneManager.getRingtone(this.getActivity(), Uri.parse(tone.toString()));
				this.mRingtone.setText(ringtone.getTitle(this.getActivity()));
			}
			
			notifyRingtoneChanged(tone);
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
		final View view =  inflater.inflate(R.layout.ringtone_fragment, container, false);
		final AudioManager audioManager = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);
		
		if(Debug.DEBUG)
			for(Entry<String,Object> item : this.mInfo.valueSet())
				Log.d(TAG, item.getKey() +" = "+ item.getValue());
		
		/*
		 * initialize the views
		 */
		final TextView label = (TextView) view.findViewById(R.id.label);
		label.setText(mLabel);
		
		this.mRingtone = (EditText) view.findViewById(R.id.ringtone);
		mVolume = (SeekBar) view.findViewById(R.id.ringtone_volume);
		
		this.mRingtone.setOnClickListener(this);
		mVolume.setMax(audioManager.getStreamMaxVolume(mStream));
		
		/*
		 * ringtone & uri
		 */
		if(this.mInfo.containsKey(this.mKeyUri) && this.mInfo.containsKey(this.mKeyRingtone)){
			this.mRingtone.setText(this.mInfo.getAsString(this.mKeyRingtone));
			this.mRingtoneURI = this.mInfo.getAsString(this.mKeyUri);
			this.mVolume.setEnabled(this.mInfo.getAsString(this.mKeyUri) != null);
		} else
			try {
				notifyRingtoneChanged(RingtoneManager.getActualDefaultRingtoneUri(this.getActivity(), mType));
				this.mRingtoneURI = RingtoneManager.getActualDefaultRingtoneUri(this.getActivity(), mType).toString();
				this.mRingtone.setText(RingtoneManager.getRingtone(this.getActivity(), Uri.parse(mRingtoneURI)).getTitle(this.getActivity()));
			} catch (NullPointerException e) {
				mVolume.setEnabled(false);
				mRingtone.setText(R.string.silent);
				mVolume.setProgress(0);
			}
			
		/*
		 * volume
		 */
		if(this.mInfo.containsKey(this.mKeyVolume))
			mVolume.setProgress(Integer.parseInt(this.mInfo.getAsString(this.mKeyVolume)));
		else {
			mVolume.setProgress(audioManager.getStreamVolume(mStream));
			notifyVolumeChanged(audioManager.getStreamVolume(mStream));
		}
		
		mVolume.setOnSeekBarChangeListener(this);
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser)
			notifyVolumeChanged(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
}