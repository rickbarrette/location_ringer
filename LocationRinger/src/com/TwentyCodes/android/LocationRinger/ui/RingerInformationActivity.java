/**
 * RingerInformationActivity.java
 * @date Aug 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import java.util.ArrayList;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.TwentyCodes.android.LocationRinger.EnableScrollingListener;
import com.TwentyCodes.android.LocationRinger.LocationRinger;
import com.TwentyCodes.android.LocationRinger.OnContentChangedListener;
import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.ui.fragments.AboutRingerFragment;
import com.TwentyCodes.android.LocationRinger.ui.fragments.FeatureListFragment;
import com.TwentyCodes.android.LocationRinger.ui.fragments.LocationInfomationFragment;
import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;
import com.jakewharton.android.viewpagerindicator.TitledFragmentAdapter;

/**
 * This activity will handle displaying ringer options
 * @author ricky
 */
public class RingerInformationActivity extends FragmentActivity implements OnContentChangedListener, EnableScrollingListener, OnPageChangeListener{

	private static final String TAG = "RingerInformationActivity";
	private ContentValues mRinger;
	private ContentValues mInfo;
	private Intent mData;
	private ViewPager mPager;

	/**
	 * Logs the content values
	 * @param values
	 * @author ricky barrette
	 */
	private void logContentValues(ContentValues values) {
		for(Entry<String,Object> item : values.valueSet())
			Log.d(TAG, item.getKey() +" = "+ item.getValue());
	}
	
	/**
	 * Called when the activity is first created
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.ringer_information_activity);
		
		/*
		 * Set up the action bar if required
		 */
		if(Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 11)
			getActionBar().setDisplayHomeAsUpEnabled(true);
		
		this.mData = new Intent().putExtras(RingerInformationActivity.this.getIntent());
		
		this.mRinger = this.mData.getParcelableExtra(ListActivity.KEY_RINGER);
		this.mInfo = this.mData.getParcelableExtra(ListActivity.KEY_INFO);

		if(this.mRinger == null)
			this.mRinger = new ContentValues();
		if(this.mInfo == null)
			this.mInfo = new ContentValues();

		/*
		 * set the title
		 */
		this.setTitle(this.mRinger.containsKey(RingerDatabase.KEY_RINGER_NAME) ?this.getString(R.string.editing)+" "+this.mRinger.getAsString(RingerDatabase.KEY_RINGER_NAME)
				: getString(R.string.new_ringer));
		
		boolean isDefault = getString(R.string.default_ringer).equals(this.mRinger.getAsString(RingerDatabase.KEY_RINGER_NAME));
		
		/*
		 * Page titles
		 */
		String[] titles = this.getResources().getStringArray(isDefault ? R.array.ringer_info_titles_default : R.array.ringer_info_titles);
		
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		
		/*
		 * about page
		 */
		if(!isDefault)
			fragments.add(new AboutRingerFragment(this.mRinger, this.mInfo, this));
		
		/*
		 * Location page
		 */
		if(!isDefault)
			fragments.add(new LocationInfomationFragment(this.mInfo, this, this));
		
		fragments.add(new FeatureListFragment(this.mInfo, this));	
		
		//Populate the pager
		this.mPager = (ViewPager)findViewById(R.id.pager);
		if(this.mPager != null)
			this.mPager.setAdapter(new TitledFragmentAdapter(this.getSupportFragmentManager(), fragments, titles));
		
		//populate the pager's indicator
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		if(indicator != null)
			indicator.setViewPager(this.mPager);
		
		indicator.setOnPageChangeListener(this);
	}

	/**
	 * Creates the main menu that is displayed when the menu button is clicked
	 * @author ricky barrette
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ringer_info_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onInfoContentChanged(ContentValues values) {
		if(Debug.DEBUG){
			Log.v(TAG,"onInfoContentChanged()");
			logContentValues(values);
		}
		this.mInfo.putAll(values);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * @author ricky barrette
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()){
			case R.id.save:
				save();
				break;
			case android.R.id.home:
	            final Intent intent = new Intent(this, LocationRinger.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra(ListActivity.NO_SPLASH, ListActivity.NO_SPLASH);
	            startActivity(intent);
	            return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * called when the pager's page is changed
	 * we use this to dismiss the soft keyboard
	 * (non-Javadoc)
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mPager.getWindowToken(), 0);
	}

	@Override
	public void onPageSelected(int arg0) {
	}

	@Override
	public void onRingerContentChanged(ContentValues values) {
		if(Debug.DEBUG){
			Log.v(TAG,"onRingerContentChanged()");
			logContentValues(values);
		}
		this.mRinger.putAll(values);		
	}

	/**
	 * Prepares a bundle containing all the information that needs to be saved, and returns it to the starting activity
	 * @author ricky barrette
	 */
	private void save() {
		final ProgressDialog progress = ProgressDialog.show(this, "", this.getText(R.string.saving), true, true);
		
		//Generate the intent in a thread to prevent anr's and allow for progress dialog
		 new Thread( new Runnable(){
			 @Override
			 public void run(){
				 Looper.prepare();
				 RingerInformationActivity.this.mData.putExtra(ListActivity.KEY_RINGER, RingerInformationActivity.this.mRinger).putExtra(ListActivity.KEY_INFO, RingerInformationActivity.this.mInfo);				 
				 RingerInformationActivity.this.setResult(Activity.RESULT_OK, RingerInformationActivity.this.mData);
				 progress.dismiss();
				 RingerInformationActivity.this.finish();
			 }
		 }).start();
	}
	
	@Override
	public void setScrollEnabled(boolean enabled) {
		this.mPager.setScrollEnabled(enabled);
	}

	@Override
	public void onInfoContentRemoved(String... keys) {
		for(String key : keys)
			if(this.mInfo.containsKey(key))
				this.mInfo.remove(key);
	}

}