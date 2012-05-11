/**
 * FragmentListAdaptor.java
 * @date Dec 24, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.LocationRinger.ui.fragments;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This Adaptor Class will be used to display fragments in a ListFragment.
 * TODO
 * + Get this code working
 * + Add/Remove fragments on the fly
 * @author ricky barrette
 */
public class FragmentListAdaptor extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<Fragment> mFragments;
	private FragmentManager mFragmentManager;
	private final String TAG = "FragmentListAdaptor";
	
	/**
	 * Creates a new FragmentListAdaptor
	 * @param listFragment
	 * @param fragments
	 * @author ricky barrette
	 */
	public FragmentListAdaptor(ListFragment listFragment, ArrayList<Fragment> fragments) {
		if(Debug.DEBUG)
			Log.v(TAG, "FragmentListAdaptor()");
		mInflater = LayoutInflater.from(listFragment.getActivity());
		mFragments = fragments;
		mFragmentManager = listFragment.getFragmentManager();
	}

	/**
	 * Returns the number of Fragments to display
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		if(Debug.DEBUG)
			Log.v(TAG, "getCount() :"+ mFragments.size());
		return mFragments.size();
	}

	/**
	 * Returns the fragment to display
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int position) {
		if(Debug.DEBUG)
			Log.v(TAG, "getItem("+position+")");
		return mFragments.get(position);
	}

	/**
	 * Returns the id of the fragment being displayed
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(Debug.DEBUG)
			Log.v(TAG, "getView("+position+")");
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		
		 // A Holder keeps references to children views to avoid unnecessary calls to findViewById() on each row.
        Holder holder;

        /*
         *  When convertView is not null, we can reuse it directly, there is no need
         *  to reinflate it. We only inflate a new View when the convertView supplied
         *  by ListView is null.
         */
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.fragment_container, null);
        	
            /*
             *  Creates a ViewHolder and store references 
             *  that we want to bind data to.
             */
            holder = new Holder();
            holder.view = (View) convertView.findViewById(R.id.fragment_container);
            holder.view.setId(position+1);
            holder.tag = createTag(position);
            convertView.setTag(holder);
            
            //add the fragment to the new view
//            transaction.add(holder.view.getId(), getItem(position), holder.tag);
            transaction.replace(holder.view.getId(), getItem(position),holder.tag);
        } else {
            // Get the ViewHolder back to get fast access to the Old Views
            holder = (Holder) convertView.getTag();
            Fragment shown = mFragmentManager.findFragmentByTag(holder.tag);
                       
            //replace the old fragment with a new one
            transaction.addToBackStack(holder.tag);
            holder.tag = createTag(position);
            if(shown != null)
            	transaction.remove(shown);
//            transaction.add(holder.view.getId(), getItem(position), holder.tag);
            transaction.replace(holder.view.getId(), getItem(position),holder.tag);
        }
		transaction.commit();

		return convertView;
	}
	
	/**
	 * @param position
	 * @return a unique tag to be used for identifying fragments
	 * @author ricky barrette
	 */
	private String createTag(int position){
		return "andorid:FragmentList:tag:"+position;
	}
	
	/**
	 * Simple Holder class
	 * @author ricky barrette
	 */
	class Holder{
		public String tag;
		public View view;
	}

}