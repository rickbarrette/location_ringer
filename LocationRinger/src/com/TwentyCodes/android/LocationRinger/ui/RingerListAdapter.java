/**
 * RingerListAdaptor.java
 * @date May 11, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This adapter will be used to populate the list view with all the ringers names, and manage enabling/disabling of ringers based on their check box.
 * @author ricky barrette
 */
public class RingerListAdapter extends BaseAdapter {

	private static final String TAG = "RingerListAdapter";

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#notifyDataSetChanged()
	 * @author ricky barrette
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	private RingerDatabase mDb;
	private List<String> mList;
	private LayoutInflater mInflater;

	/**
	 * Creates a new RingerListAdapter
	 * @param context
	 * @param listener
	 * @param db
	 * @author ricky barrette
	 */
	public RingerListAdapter(Context context, RingerDatabase db) {
		super();
		// Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
		mDb = db;
		mList = db.getAllRingerTitles();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unused")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		 // A ViewHolder keeps references to children views to avoid unnecessary calls to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.list_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.ringer_enabled_checkbox);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }
        
        if(Debug.DEBUG){
        	Log.d(TAG, "postion = "+position);

	        if(convertView == null)
	        	Log.e(TAG,"convertview is null!!!");
	        
	        if(holder == null)
	        	Log.e(TAG,"holder is null!!!");
	        
	        if(holder.text == null)
	        	Log.e(TAG,"holder.text is null!!!");
	        
	        if(holder.checkbox == null)
	        	Log.e(TAG,"holder.checkbox is null!!!");
        }
        
        /*
         * Bind the data efficiently with the holder.
         * Remember that you should always call setChecked() after calling setOnCheckedChangedListener.
         * This will prevent the list from changing the values on you.
         */
        holder.text.setText(getItem(position));
        holder.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				mDb.setRingerEnabled(position +1, isChecked);
			}
		});
        holder.checkbox.setChecked(mDb.isRingerEnabled(position +1));
        
        //Remove the checkbox for the default ringer
        if(position == 0)
        	holder.checkbox.setVisibility(View.INVISIBLE);
        else
        	holder.checkbox.setVisibility(View.VISIBLE);
        return convertView;
	}
	
	class ViewHolder {
        TextView text;
        CheckBox checkbox;
    }

}