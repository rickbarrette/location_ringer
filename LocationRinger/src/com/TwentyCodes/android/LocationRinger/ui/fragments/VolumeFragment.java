/**
 * AlarmVolumeFragment.java
 * @date Aug 7, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.TwentyCodes.android.LocationRinger.FeatureRemovedListener;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will represent the volume fragments
 * @author ricky
 */
public class VolumeFragment extends BaseFeatureFragment implements OnSeekBarChangeListener {

	private static final String TAG = "VolumeFragment";
	private final AudioManager mAudioManager;
	private final int mStream;
	private final OnContentChangedListener mChangedListener;
	private final String mKey;
	private final ContentValues mInfo;
	private final int mLabel;
	
	/**
	 * Creates a new Volume Fragment
	 * @param info
	 * @param context
	 * @param changedListener
	 * @param stream
	 * @author ricky barrette
	 */
	public VolumeFragment(ContentValues info, Context context, OnContentChangedListener changedListener, FeatureRemovedListener removedListener, int stream, int id){
		super(id, R.layout.volume_fragment, removedListener);
		
		if ( info == null )
			throw new NullPointerException();
		if ( context == null )
			throw new NullPointerException();
		if ( changedListener == null )
			throw new NullPointerException();
		
		this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		this.mStream = stream;
		this.mChangedListener = changedListener;
		this.mInfo = info;
		
		switch(this.mStream){
			case AudioManager.STREAM_ALARM:
				this.mLabel = R.string.alarm_volume;
				this.mKey = RingerDatabase.KEY_ALARM_VOLUME;
				break;
			case AudioManager.STREAM_DTMF:
				this.mLabel = R.string.dtmf_volume;
				this.mKey = RingerDatabase.KEY_DTMF_VOLUME;
				break;
			case AudioManager.STREAM_MUSIC:
				this.mLabel = R.string.music_volume;
				this.mKey = RingerDatabase.KEY_MUSIC_VOLUME;
				break;
			case AudioManager.STREAM_NOTIFICATION:
				this.mLabel = R.string.notification_volume;
				this.mKey = RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME;
				break;
			case AudioManager.STREAM_RING:
				this.mLabel = R.string.ringtone_volume;
				this.mKey = RingerDatabase.KEY_RINGTONE_VOLUME;
				break;
			case AudioManager.STREAM_SYSTEM:
				this.mLabel = R.string.system_volume;
				this.mKey = RingerDatabase.KEY_SYSTEM_VOLUME;
				break;
			case AudioManager.STREAM_VOICE_CALL:
				this.mLabel =  R.string.call_volume;
				this.mKey = RingerDatabase.KEY_CALL_VOLUME;
				break;
			default:
				this.mLabel = R.string.volume;
				this.mKey = RingerDatabase.KEY_RINGTONE_VOLUME;
				break;
		}
	}

	/**
	 * Called when the fragment's view needs to be created
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if(Debug.DEBUG)
			for(Entry<String,Object> item : this.mInfo.valueSet())
				Log.d(TAG, item.getKey() +" = "+ item.getValue());
		
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final TextView label = (TextView) view.findViewById(R.id.title);
		final SeekBar volume = (SeekBar) view.findViewById(R.id.volume);
		volume.setMax(this.mAudioManager.getStreamMaxVolume(mStream));
		volume.setProgress(this.mAudioManager.getStreamVolume(mStream));
		volume.setOnSeekBarChangeListener(this);
		
		label.setText(mLabel);
		
		if(this.mInfo.containsKey(this.mKey))
			volume.setProgress(Integer.parseInt(this.mInfo.getAsString(this.mKey)));
		else
			notifyListener(this.mAudioManager.getStreamVolume(mStream));
		
		setIcon(volume.getProgress() == 0 ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off);
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser){
			notifyListener(progress);
			setIcon(progress == 0 ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off);
		}
	}

	/**
	 * Notifys the listener of changes made to the volume
	 * @param progress
	 * @author ricky barrette
	 */
	private void notifyListener(final int progress) {
		if(this.mChangedListener != null){
			final ContentValues info = new ContentValues();
			info.put(this.mKey, progress);
			this.mChangedListener.onInfoContentChanged(info);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
}