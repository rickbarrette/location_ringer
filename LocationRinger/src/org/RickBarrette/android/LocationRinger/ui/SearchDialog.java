/**
 * SearchDialog.java
 * @date May 9, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.RickBarrette.android.LocationRinger.Log;
import org.RickBarrette.android.LocationRinger.R;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
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

import com.TwentyCodes.android.location.OnLocationSelectedListener;
import com.TwentyCodes.android.location.ReverseGeocoder;
import com.google.android.maps.GeoPoint;

/**
 * This dialog will be used to get users input for the address that they want to
 * search for. A GeoPoint location will be returned via LocationSelectedListener
 * 
 * @author ricky barrette
 */
public class SearchDialog extends Dialog implements android.view.View.OnClickListener, OnItemClickListener, OnEditorActionListener {

	protected static final String TAG = "SearchDialog";
	private final ListView mAddressList;
	private final EditText mAddress;
	private JSONArray mResults;
	private final ProgressBar mProgress;
	private final Handler mHandler;
	private final Context mContext;
	private final OnLocationSelectedListener mListener;

	/**
	 * Creates a new search dialog
	 * 
	 * @param context
	 * @author ricky barrette
	 */
	public SearchDialog(final Context context, final OnLocationSelectedListener listener) {
		super(context);
		this.setTitle(R.string.search);
		this.setContentView(R.layout.address_dialog);
		findViewById(R.id.ok).setOnClickListener(this);
		mAddressList = (ListView) findViewById(R.id.address_list);
		mAddressList.setOnItemClickListener(this);
		mAddress = (EditText) findViewById(R.id.address);
		mAddress.setOnEditorActionListener(this);
		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		mProgress = (ProgressBar) findViewById(R.id.search_progress);
		mHandler = new Handler();
		mContext = context;
		mListener = listener;
	}

	/**
	 * Retrieves all the strings from the JSON Array
	 * 
	 * @return list of addresses
	 * @author ricky barrette
	 */
	private ArrayList<String> getAddress() {
		Log.d(TAG, "getAddress()");
		final ArrayList<String> list = new ArrayList<String>();
		try {
			for (int i = 0; i < mResults.length(); i++)
				list.add(mResults.getJSONObject(i).getString("address"));
		} catch (final JSONException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	/**
	 * Retrieves the GeoPoint from the JSON Array for the given index
	 * 
	 * @param index
	 *            for the place
	 * @return GeoPoint of the place
	 * @author ricky barrette
	 */
	private GeoPoint getCoords(final int index) {
		Log.d(TAG, "getCoords()");
		try {
			final JSONArray coords = mResults.getJSONObject(index).getJSONObject("Point").getJSONArray("coordinates");
			Log.d(TAG, "creating geopoint: " + new GeoPoint((int) (coords.getDouble(1) * 1E6), (int) (coords.getDouble(0) * 1E6)).toString());
			return new GeoPoint((int) (coords.getDouble(1) * 1E6), (int) (coords.getDouble(0) * 1E6));
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Called when the search button is clicked (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.ok:
			search();
			break;
		}
	}

	/**
	 * Called when the seach button on the soft keyboard is pressed
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView,
	 *      int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
		search();
		return false;
	}

	/**
	 * Called when an Item from the list is selected (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		Log.d(TAG, "slected " + (int) id);
		mListener.onLocationSelected(getCoords((int) id));
		dismiss();
	}

	private void search() {
		final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mAddress.getWindowToken(), 0);
		final View v = findViewById(R.id.ok);
		v.setEnabled(false);
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "strarting search and parsing");
				try {
					mResults = ReverseGeocoder.addressSearch(mAddress.getText().toString());
				} catch (final IOException e) {
					e.printStackTrace();
				} catch (final JSONException e) {
					e.printStackTrace();
				}
				if (mResults != null) {
					Log.d(TAG, "finished searching and parsing");
					// update UI
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Log.d(TAG, "populating list");
							mAddressList.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, getAddress()));
							v.setEnabled(true);
							mProgress.setVisibility(View.INVISIBLE);
							mProgress.setIndeterminate(false);
							Log.d(TAG, "finished");
						}
					});
				} else
					// update the UI
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							v.setEnabled(true);
							mProgress.setVisibility(View.INVISIBLE);
							mProgress.setIndeterminate(false);
							Log.d(TAG, "failed");
						}
					});
			}
		}).start();
	}
}