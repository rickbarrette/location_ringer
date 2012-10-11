/**
 * AlarmVolumeFragment.java
 * @date Aug 7, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.TwentyCodes.android.LocationRinger.FeatureRemovedListener;
import com.TwentyCodes.android.LocationRinger.Log;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;

/**
 * This fragment will represent the volume fragments
 * 
 * @author ricky
 */
@SuppressLint("ValidFragment")
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
	 * 
	 * @param info
	 * @param context
	 * @param changedListener
	 * @param stream
	 * @author ricky barrette
	 */
	public VolumeFragment(final ContentValues info, final Context context, final OnContentChangedListener changedListener, final FeatureRemovedListener removedListener,
			final int stream, final int id) {
		super(id, R.layout.volume_fragment, removedListener);

		if (info == null)
			throw new NullPointerException();
		if (context == null)
			throw new NullPointerException();
		if (changedListener == null)
			throw new NullPointerException();

		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mStream = stream;
		mChangedListener = changedListener;
		mInfo = info;

		switch (mStream) {
		case AudioManager.STREAM_ALARM:
			mLabel = R.string.alarm_volume;
			mKey = RingerDatabase.KEY_ALARM_VOLUME;
			break;
		case AudioManager.STREAM_DTMF:
			mLabel = R.string.dtmf_volume;
			mKey = RingerDatabase.KEY_DTMF_VOLUME;
			break;
		case AudioManager.STREAM_MUSIC:
			mLabel = R.string.music_volume;
			mKey = RingerDatabase.KEY_MUSIC_VOLUME;
			break;
		case AudioManager.STREAM_NOTIFICATION:
			mLabel = R.string.notification_volume;
			mKey = RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME;
			break;
		case AudioManager.STREAM_RING:
			mLabel = R.string.ringtone_volume;
			mKey = RingerDatabase.KEY_RINGTONE_VOLUME;
			break;
		case AudioManager.STREAM_SYSTEM:
			mLabel = R.string.system_volume;
			mKey = RingerDatabase.KEY_SYSTEM_VOLUME;
			break;
		case AudioManager.STREAM_VOICE_CALL:
			mLabel = R.string.call_volume;
			mKey = RingerDatabase.KEY_CALL_VOLUME;
			break;
		default:
			mLabel = R.string.volume;
			mKey = RingerDatabase.KEY_RINGTONE_VOLUME;
			break;
		}
	}

	/**
	 * Notifys the listener of changes made to the volume
	 * 
	 * @param progress
	 * @author ricky barrette
	 */
	private void notifyListener(final int progress) {
		if (mChangedListener != null) {
			final ContentValues info = new ContentValues();
			info.put(mKey, progress);
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

		for (final Entry<String, Object> item : mInfo.valueSet())
			Log.d(TAG, item.getKey() + " = " + item.getValue());

		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final TextView label = (TextView) view.findViewById(R.id.title);
		final SeekBar volume = (SeekBar) view.findViewById(R.id.volume);
		volume.setMax(mAudioManager.getStreamMaxVolume(mStream));
		volume.setProgress(mAudioManager.getStreamVolume(mStream));
		volume.setOnSeekBarChangeListener(this);

		label.setText(mLabel);

		if (mInfo.containsKey(mKey))
			volume.setProgress(Integer.parseInt(mInfo.getAsString(mKey)));
		else
			notifyListener(mAudioManager.getStreamVolume(mStream));

		setIcon(volume.getProgress() == 0 ? R.drawable.ic_action_silent : R.drawable.ic_action_volume);
		return view;
	}

	@Override
	public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
		if (fromUser) {
			notifyListener(progress);
			setIcon(progress == 0 ? R.drawable.ic_action_silent : R.drawable.ic_action_volume);
		}
	}

	@Override
	public void onStartTrackingTouch(final SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(final SeekBar seekBar) {
	}
}