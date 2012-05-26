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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.TwentyCodes.android.LocationRinger.FeatureRemovedListener;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will be for ringtone settings
 * @author ricky
 */
public class RingtoneFragment extends IdFragment implements OnClickListener, OnSeekBarChangeListener {
	
	private static final String TAG = "RingtoneFragment";
	private final int mStream;
	private final int mType;
	private final OnContentChangedListener mChangedListener;
	private final String mKeyEnabled;
	private final String mKeyUri;
	private final String mKeyVolume;
	private final ContentValues mInfo;
	private final FeatureRemovedListener mRemovedListener;
	private final int mLabel;
	private EditText mRingtone;
	private Uri mRingtoneURI;
	private SeekBar mVolume;
	private ImageView mIcon;
	
	public RingtoneFragment(ContentValues info, OnContentChangedListener changedListener, FeatureRemovedListener removedListener, int stream, int id){
		super(id);
		
		if ( info == null )
			throw new NullPointerException();
		if ( changedListener == null )
			throw new NullPointerException();
		if ( removedListener == null )
			throw new NullPointerException();
		
		this.mChangedListener = changedListener;
		this.mStream = stream;
		this.mInfo = info;
		this.mRemovedListener = removedListener;
		
		switch(stream){
			case AudioManager.STREAM_NOTIFICATION:
				mKeyEnabled = RingerDatabase.KEY_NOTIFICATION_IS_SILENT;
				mKeyUri = RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI;
				mKeyVolume = RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME;
				mLabel = R.string.notification_ringtone;
				mType = RingtoneManager.TYPE_NOTIFICATION;
				break;
				
			case AudioManager.STREAM_RING:
			default:
				mKeyEnabled = RingerDatabase.KEY_RINGTONE_IS_SILENT;
				mKeyUri = RingerDatabase.KEY_RINGTONE_URI;
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
	private void getRingtoneURI(final int ringtoneCode, final Uri uri){
        final Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneCode);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, R.string.select_tone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
        startActivityForResult( intent, ringtoneCode);  
	}
	
	/**
	 * Notifys the listener that the ringtone has changedRingtoneManager.getActualDefaultRingtoneUri(this.getActivity(), mType)
	 * @param tone
	 * @author ricky barrette
	 */
	private void notifyRingtoneChanged(Uri tone) {
		if(this.mChangedListener != null){
			ContentValues info = new ContentValues();			
			info.put(this.mKeyUri, tone != null ? tone.toString() : null);
			info.put(mKeyEnabled, tone == null);
			this.mChangedListener.onInfoContentChanged(info);
		}
	}
	
	/**
	 * Notifys the listener that the volume has changed
	 * @param progress
	 * @author ricky barrette
	 */
	private void notifyVolumeChanged(int progress) {
		mIcon.setImageDrawable(this.getActivity().getResources().getDrawable(progress == 0 ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off));
		if(this.mChangedListener != null){
			final ContentValues info = new ContentValues();
			info.put(this.mKeyVolume, progress);
			this.mChangedListener.onInfoContentChanged(info);
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
				notifyVolumeChanged(0);
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
		switch(v.getId()){
		case R.id.ringtone:
			getRingtoneURI(this.mType, mRingtoneURI);
			break;
		case R.id.close:
			if(this.mRemovedListener != null)
				this.mRemovedListener.onFeatureRemoved(this);
			break;
			
		}
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
		final TextView label = (TextView) view.findViewById(R.id.title);
		label.setText(mLabel);
		
		mIcon = (ImageView) view.findViewById(R.id.icon);
		mIcon.setImageDrawable(this.getActivity().getResources().getDrawable(android.R.drawable.ic_lock_silent_mode_off));
		
		this.mRingtone = (EditText) view.findViewById(R.id.ringtone);
		mVolume = (SeekBar) view.findViewById(R.id.ringtone_volume);
		
		this.mRingtone.setOnClickListener(this);
		mVolume.setMax(audioManager.getStreamMaxVolume(mStream));
		
		view.findViewById(R.id.close).setOnClickListener(this);
		
		/*
		 * volume
		 */
		if(this.mInfo.containsKey(this.mKeyVolume))
			mVolume.setProgress(Integer.parseInt(this.mInfo.getAsString(this.mKeyVolume)));
		else {
			mVolume.setProgress(audioManager.getStreamVolume(mStream));
			notifyVolumeChanged(audioManager.getStreamVolume(mStream));
		}
		
		/*
		 * ringtone & uri
		 */
		if(this.mInfo.containsKey(this.mKeyUri)){
			try{
				this.mRingtoneURI = Uri.parse(this.mInfo.getAsString(this.mKeyUri));
			} catch (NullPointerException e){
				this.mRingtoneURI = null;
			}
			this.mVolume.setEnabled(this.mInfo.getAsString(this.mKeyUri) != null);
		} else {
			this.mRingtoneURI = RingtoneManager.getActualDefaultRingtoneUri(this.getActivity(), mType);
			notifyRingtoneChanged(this.mRingtoneURI);
		}

		try {
			this.mRingtone.setText(RingtoneManager.getRingtone(this.getActivity(), mRingtoneURI).getTitle(this.getActivity()));
		} catch (NullPointerException e) {
			mVolume.setEnabled(false);
			mRingtone.setText(R.string.silent);
			mVolume.setProgress(0);
		}
		
		mIcon.setImageDrawable(this.getActivity().getResources().getDrawable(mVolume.getProgress() == 0 ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off));
		
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