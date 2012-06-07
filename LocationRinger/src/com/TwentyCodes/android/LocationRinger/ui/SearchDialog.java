/**
 * SearchDialog.java
 * @date May 9, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.location.OnLocationSelectedListener;
import com.TwentyCodes.android.location.ReverseGeocoder;
import com.google.android.maps.GeoPoint;

/**
 * This dialog will be used to get users input for the address that they want to search for. A GeoPoint location will be returned via LocationSelectedListener
 * @author ricky barrette
 */
public class SearchDialog extends Dialog implements android.view.View.OnClickListener, OnItemClickListener, OnEditorActionListener{

	protected static final String TAG = "SearchDialog";
	private ListView mAddressList;
	private EditText mAddress;
	private JSONArray mResults;
	private ProgressBar mProgress;
	private Handler mHandler;
	private Context mContext;
	private OnLocationSelectedListener mListener;

	/**
	 * Creates a new search dialog
	 * @param context
	 * @author ricky barrette
	 */
	public SearchDialog(Context context, OnLocationSelectedListener listener) {
		super(context);
		this.setTitle(R.string.search);
		this.setContentView(R.layout.address_dialog);
		findViewById(R.id.ok).setOnClickListener(this);
		mAddressList = (ListView) findViewById(R.id.address_list);
		mAddressList.setOnItemClickListener(this);
		mAddress = (EditText) findViewById(R.id.address);
		mAddress.setOnEditorActionListener(this);
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		mProgress = (ProgressBar) findViewById(R.id.search_progress);
		mHandler = new Handler();
		mContext = context;
		mListener = listener;
	}


	/**
	 * Retrieves all the strings from the JSON Array
	 * @return list of addresses
	 * @author ricky barrette
	 */
	private ArrayList<String> getAddress() {
		if(Debug.DEBUG)
			Log.d(TAG,"getAddress()");
		ArrayList<String> list = new ArrayList<String>();
		try {
			for(int i = 0; i < mResults.length(); i++){
				list.add(mResults.getJSONObject(i).getString("address"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	/**
	 * Retrieves the GeoPoint from the JSON Array for the given index
	 * @param index for the place
	 * @return GeoPoint of the place
	 * @author ricky barrette
	 */
	private GeoPoint getCoords(int index){
		if(Debug.DEBUG)
			Log.d(TAG,"getCoords()");
		try {
			JSONArray coords = mResults.getJSONObject(index).getJSONObject("Point").getJSONArray("coordinates");
			if(Debug.DEBUG)
				Log.d(TAG,"creating geopoint: "+ new GeoPoint((int) (coords.getDouble(1) *1E6), (int) (coords.getDouble(0)*1E6)).toString());
			return new GeoPoint((int) (coords.getDouble(1) *1E6), (int) (coords.getDouble(0)*1E6));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Called when the search button is clicked
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(final View v) {
		switch(v.getId()){
			case R.id.ok:
				search();
				break;
		}
	}
	
	/**
	 * Called when the seach button on the soft keyboard is pressed
	 * (non-Javadoc)
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		search();
		return false;
	}

	/**
	 * Called when an Item from the list is selected
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(Debug.DEBUG)
			Log.d(TAG,"slected "+ (int) id);
		mListener.onLocationSelected(getCoords((int) id));
		this.dismiss();
	}

	private void search() {
		final InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.mAddress.getWindowToken(), 0);
		final View v = this.findViewById(R.id.ok);
		v.setEnabled(false);
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(true);
		new Thread( new Runnable(){
			@Override
			public void run(){
				if(Debug.DEBUG)
					Log.d(TAG,"strarting search and parsing") ;
				try {
					mResults = ReverseGeocoder.addressSearch(mAddress.getText().toString());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(mResults != null){
					if(Debug.DEBUG)
						Log.d(TAG,"finished searching and parsing");
					//update UI
					mHandler.post(new Runnable(){
						@Override
						public void run(){
							if(Debug.DEBUG)
								Log.d(TAG,"populating list");
							mAddressList.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, getAddress()));
							v.setEnabled(true);
							mProgress.setVisibility(View.INVISIBLE);
							mProgress.setIndeterminate(false);
							if(Debug.DEBUG)
								Log.d(TAG,"finished");
						}
					});
				} else {
					//update the UI
					mHandler.post(new Runnable(){
						@Override
						public void run(){
							v.setEnabled(true);
							mProgress.setVisibility(View.INVISIBLE);
							mProgress.setIndeterminate(false);
							if(Debug.DEBUG)
								Log.d(TAG,"failed");
						}
					});
				}
			}
		}).start();
	}
}