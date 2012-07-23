/**
 * RingtoneFragment.java
 * @date Aug 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.Map.Entry;

import android.annotation.SuppressLint;
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
 * 
 * @author ricky
 */
@SuppressLint("ValidFragment")
public class RingtoneFragment extends BaseFeatureFragment implements OnClickListener, OnSeekBarChangeListener {

	private static final String TAG = "RingtoneFragment";
	private final int mStream;
	private final int mType;
	private final OnContentChangedListener mChangedListener;
	private final String mKeyUri;
	private final String mKeyVolume;
	private final ContentValues mInfo;
	private final int mLabel;
	private EditText mRingtone;
	private Uri mRingtoneURI;
	private SeekBar mVolume;

	public RingtoneFragment(final ContentValues info, final OnContentChangedListener changedListener, final FeatureRemovedListener removedListener, final int stream, final int id) {
		super(id, R.layout.ringtone_fragment, removedListener);

		if (info == null)
			throw new NullPointerException();
		if (changedListener == null)
			throw new NullPointerException();

		mChangedListener = changedListener;
		mStream = stream;
		mInfo = info;

		switch (stream) {
		case AudioManager.STREAM_NOTIFICATION:
			mKeyUri = RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI;
			mKeyVolume = RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME;
			mLabel = R.string.notification_ringtone;
			mType = RingtoneManager.TYPE_NOTIFICATION;
			break;

		case AudioManager.STREAM_RING:
		default:
			mKeyUri = RingerDatabase.KEY_RINGTONE_URI;
			mKeyVolume = RingerDatabase.KEY_RINGTONE_VOLUME;
			mLabel = R.string.ringtone;
			mType = RingtoneManager.TYPE_RINGTONE;
			break;
		}
	}

	/**
	 * starts the ringtone picker
	 * 
	 * @param ringtoneCode
	 *            RingtoneManager.TYPE_?
	 * @param uri
	 *            of current tone
	 * @author ricky barrette
	 */
	private void getRingtoneURI(final int ringtoneCode, final Uri uri) {
		final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneCode);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, R.string.select_tone);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
		startActivityForResult(intent, ringtoneCode);
	}

	/**
	 * Notifys the listener that the ringtone has
	 * changedRingtoneManager.getActualDefaultRingtoneUri(this.getActivity(),
	 * mType)
	 * 
	 * @param tone
	 * @author ricky barrette
	 */
	private void notifyRingtoneChanged(final Uri tone) {
		if (mChangedListener != null) {
			final ContentValues info = new ContentValues();
			info.put(mKeyUri, tone != null ? tone.toString() : null);
			mChangedListener.onInfoContentChanged(info);
		}
	}

	/**
	 * Notifys the listener that the volume has changed
	 * 
	 * @param progress
	 * @author ricky barrette
	 */
	private void notifyVolumeChanged(final int progress) {
		setIcon(progress == 0 ? R.drawable.ic_action_silent : R.drawable.ic_action_volume);
		if (mChangedListener != null) {
			final ContentValues info = new ContentValues();
			info.put(mKeyVolume, progress);
			mChangedListener.onInfoContentChanged(info);
		}
	}

	/**
	 * Called when the ringtone picker activity returns it's result
	 */
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			final Uri tone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (tone == null) {
				mRingtone.setText(R.string.silent);
				mVolume.setEnabled(false);
				mVolume.setProgress(0);
				notifyVolumeChanged(0);
			} else {
				mVolume.setEnabled(true);
				final Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(tone.toString()));
				mRingtone.setText(ringtone.getTitle(getActivity()));
			}

			notifyRingtoneChanged(tone);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Called when a view is clicked
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.ringtone:
			getRingtoneURI(mType, mRingtoneURI);
			break;
		default:
			super.onClick(v);
			break;
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

		if (Debug.DEBUG)
			for (final Entry<String, Object> item : mInfo.valueSet())
				Log.d(TAG, item.getKey() + " = " + item.getValue());

		/*
		 * initialize the views
		 */
		final TextView label = (TextView) view.findViewById(R.id.title);
		label.setText(mLabel);

		setIcon(R.drawable.ic_action_volume);

		mRingtone = (EditText) view.findViewById(R.id.ringtone);
		mVolume = (SeekBar) view.findViewById(R.id.ringtone_volume);

		mRingtone.setOnClickListener(this);
		mVolume.setMax(audioManager.getStreamMaxVolume(mStream));

		view.findViewById(R.id.close).setOnClickListener(this);

		/*
		 * volume
		 */
		if (mInfo.containsKey(mKeyVolume))
			mVolume.setProgress(Integer.parseInt(mInfo.getAsString(mKeyVolume)));
		else {
			mVolume.setProgress(audioManager.getStreamVolume(mStream));
			notifyVolumeChanged(audioManager.getStreamVolume(mStream));
		}

		/*
		 * ringtone & uri
		 */
		if (mInfo.containsKey(mKeyUri)) {
			try {
				mRingtoneURI = Uri.parse(mInfo.getAsString(mKeyUri));
			} catch (final NullPointerException e) {
				mRingtoneURI = null;
			}
			mVolume.setEnabled(mInfo.getAsString(mKeyUri) != null);
		} else {
			mRingtoneURI = RingtoneManager.getActualDefaultRingtoneUri(getActivity(), mType);
			notifyRingtoneChanged(mRingtoneURI);
		}

		try {
			mRingtone.setText(RingtoneManager.getRingtone(getActivity(), mRingtoneURI).getTitle(getActivity()));
		} catch (final NullPointerException e) {
			mVolume.setEnabled(false);
			mRingtone.setText(R.string.silent);
			mVolume.setProgress(0);
		}

		setIcon(mVolume.getProgress() == 0 ? R.drawable.ic_action_silent : R.drawable.ic_action_volume);

		mVolume.setOnSeekBarChangeListener(this);
		return view;
	}

	@Override
	public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
		if (fromUser)
			notifyVolumeChanged(progress);
	}

	@Override
	public void onStartTrackingTouch(final SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(final SeekBar seekBar) {
	}

}