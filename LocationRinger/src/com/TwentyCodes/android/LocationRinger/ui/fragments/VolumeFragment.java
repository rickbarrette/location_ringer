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
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This fragment will represent the volume fragments
 * @author ricky
 */
public class VolumeFragment extends Fragment implements OnSeekBarChangeListener {

	private static final String TAG = "VolumeFragment";
	private AudioManager mAudioManager;
	private int mStream;
	private OnContentChangedListener mListener;
	private String mKey;
	private ContentValues mInfo;
	
	public VolumeFragment(ContentValues info, Context context, OnContentChangedListener listener, int stream){
		super();
		this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		this.mStream = stream;
		this.mListener = listener;
		this.mInfo = info;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if(Debug.DEBUG)
			for(Entry<String,Object> item : this.mInfo.valueSet())
				Log.d(TAG, item.getKey() +" = "+ item.getValue());
		
		View view = inflater.inflate(R.layout.volume_fragment, container, false);
		TextView label = (TextView) view.findViewById(R.id.volume_label);
		SeekBar volume = (SeekBar) view.findViewById(R.id.volume);
		volume.setMax(this.mAudioManager.getStreamMaxVolume(mStream));
		volume.setProgress(this.mAudioManager.getStreamVolume(mStream));
		volume.setOnSeekBarChangeListener(this);
		
		switch(this.mStream){
			case AudioManager.STREAM_ALARM:
				label.setText(R.string.alarm_volume);
				this.mKey = RingerDatabase.KEY_ALARM_VOLUME;
				break;
			case AudioManager.STREAM_DTMF:
				label.setText(R.string.dtmf_volume);
				this.mKey = RingerDatabase.KEY_DTMF_VOLUME;
				break;
			case AudioManager.STREAM_MUSIC:
				label.setText(R.string.music_volume);
				this.mKey = RingerDatabase.KEY_MUSIC_VOLUME;
				break;
			case AudioManager.STREAM_NOTIFICATION:
				label.setText(R.string.notification_volume);
				this.mKey = RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME;
				break;
			case AudioManager.STREAM_RING:
				label.setText(R.string.ringtone_volume);
				this.mKey = RingerDatabase.KEY_RINGTONE_VOLUME;
				break;
			case AudioManager.STREAM_SYSTEM:
				label.setText(R.string.system_volume);
				this.mKey = RingerDatabase.KEY_SYSTEM_VOLUME;
				break;
			case AudioManager.STREAM_VOICE_CALL:
				label.setText(R.string.call_volume);
				this.mKey = RingerDatabase.KEY_CALL_VOLUME;
				break;
		}
		
		if(this.mInfo.containsKey(this.mKey))
			volume.setProgress(Integer.parseInt(this.mInfo.getAsString(this.mKey)));
		
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser)
			if(this.mListener != null){
				ContentValues info = new ContentValues();
				info.put(this.mKey, progress);
				this.mListener.onInfoContentChanged(info);
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

}
