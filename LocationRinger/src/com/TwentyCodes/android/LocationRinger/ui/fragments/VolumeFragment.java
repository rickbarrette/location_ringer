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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
 * This fragment will represent the volume fragments
 * @author ricky
 */
public class VolumeFragment extends IdFragment implements OnSeekBarChangeListener, OnClickListener {

	private static final String TAG = "VolumeFragment";
	private final AudioManager mAudioManager;
	private final int mStream;
	private final OnContentChangedListener mChangedListener;
	private final String mKey;
	private final ContentValues mInfo;
	private final int mLabel;
	private final FeatureRemovedListener mRemovedListener;
	private ImageView mIcon;
	
	/**
	 * Creates a new Volume Fragment
	 * @param info
	 * @param context
	 * @param changedListener
	 * @param stream
	 * @author ricky barrette
	 */
	public VolumeFragment(ContentValues info, Context context, OnContentChangedListener changedListener, FeatureRemovedListener removedListener, int stream, int id){
		super(id);
		this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		this.mStream = stream;
		this.mChangedListener = changedListener;
		this.mInfo = info;
		this.mRemovedListener = removedListener;
		
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if(Debug.DEBUG)
			for(Entry<String,Object> item : this.mInfo.valueSet())
				Log.d(TAG, item.getKey() +" = "+ item.getValue());
		
		final View view = inflater.inflate(R.layout.volume_fragment, container, false);
		final TextView label = (TextView) view.findViewById(R.id.title);
		final SeekBar volume = (SeekBar) view.findViewById(R.id.volume);
		volume.setMax(this.mAudioManager.getStreamMaxVolume(mStream));
		volume.setProgress(this.mAudioManager.getStreamVolume(mStream));
		volume.setOnSeekBarChangeListener(this);
		
		label.setText(mLabel);
		
		mIcon = (ImageView) view.findViewById(R.id.icon);
		
		view.findViewById(R.id.close).setOnClickListener(this);
		
		if(this.mInfo.containsKey(this.mKey))
			volume.setProgress(Integer.parseInt(this.mInfo.getAsString(this.mKey)));
		else
			notifyListener(this.mAudioManager.getStreamVolume(mStream));
		
		mIcon.setImageDrawable(this.getActivity().getResources().getDrawable(volume.getProgress() == 0 ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off));
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser){
			notifyListener(progress);
			mIcon.setImageDrawable(this.getActivity().getResources().getDrawable(progress == 0 ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off));
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
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the user clicks on the remove button
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if(this.mRemovedListener != null)
			this.mRemovedListener.onFeatureRemoved(this);
	}
}