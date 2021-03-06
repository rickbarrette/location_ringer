/**
 * RingerInformationActivity.java
 * @date Aug 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.ui;

import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import com.TwentyCodes.android.exception.ExceptionHandler;
import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;
import com.jakewharton.android.viewpagerindicator.TitledFragmentAdapter;
import org.RickBarrette.android.LocationRinger.*;
import org.RickBarrette.android.LocationRinger.db.RingerDatabase;
import org.RickBarrette.android.LocationRinger.ui.fragments.AboutRingerFragment;
import org.RickBarrette.android.LocationRinger.ui.fragments.FeatureListFragment;
import org.RickBarrette.android.LocationRinger.ui.fragments.LocationInformationFragment;
import org.RickBarrette.android.LocationRinger.ui.fragments.RingtoneFragment;

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * This activity will handle displaying ringer options
 * 
 * @author ricky
 */
public class RingerInformationActivity extends FragmentActivity implements OnContentChangedListener, EnableScrollingListener, OnPageChangeListener {

	private static final String TAG = "RingerInformationActivity";
	private ContentValues mRinger;
	private ContentValues mInfo;
	private Intent mData;
	private ViewPager mPager;
	private LocationInformationFragment mLocationInfomationFragment;
	private Fragment mFragmentCallBack;

	/**
	 * Logs the content values
	 * 
	 * @param values
	 * @author ricky barrette
	 */
	private void logContentValues(final ContentValues values) {
		if(Constraints.VERBOSE)
			for (final Entry<String, Object> item : values.valueSet())
				Log.v(TAG, item.getKey() + " = " + item.getValue());
	}

	/**
	 * Called when the activity is first created (non-Javadoc)
	 * 
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(final Bundle arg0) {
		super.onCreate(arg0);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

		final Intent intent = getIntent();

		setContentView(R.layout.ringer_information_activity);

		/*
		 * Set up the action bar if required
		 */
		if (Constraints.SUPPORTS_HONEYCOMB)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		mData = new Intent().putExtras(intent);

		mRinger = mData.getParcelableExtra(ListActivity.KEY_RINGER);
		mInfo = mData.getParcelableExtra(ListActivity.KEY_INFO);

		if (mRinger == null)
			mRinger = new ContentValues();
		if (mInfo == null)
			mInfo = new ContentValues();

		/*
		 * set the title
		 */
		this.setTitle(mRinger.containsKey(RingerDatabase.KEY_RINGER_NAME) ? this.getString(R.string.editing) + " " + mRinger.getAsString(RingerDatabase.KEY_RINGER_NAME)
				: getString(R.string.new_ringer));

		final boolean isDefault = getString(R.string.default_ringer).equals(mRinger.getAsString(RingerDatabase.KEY_RINGER_NAME));

		/*
		 * Page titles
		 */
		final String[] titles = getResources().getStringArray(isDefault ? R.array.ringer_info_titles_default : R.array.ringer_info_titles);

		final ArrayList<Fragment> fragments = new ArrayList<Fragment>();

		/*
		 * about page
		 */
		if (!isDefault)
			fragments.add(new AboutRingerFragment(mRinger, mInfo, this));

		/*
		 * Location page
		 */
		if (!isDefault) {
			mLocationInfomationFragment = new LocationInformationFragment(mInfo, this, this);
			fragments.add(mLocationInfomationFragment);
		}

		fragments.add(new FeatureListFragment(mInfo, this));

		// Populate the pager
		mPager = (ViewPager) findViewById(R.id.pager);
		if (mPager != null)
			mPager.setAdapter(new TitledFragmentAdapter(getSupportFragmentManager(), fragments, titles));

		// populate the pager's indicator
		final TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		if (indicator != null)
			indicator.setViewPager(mPager);

		indicator.setOnPageChangeListener(this);
	}

	/**
	 * Creates the main menu that is displayed when the menu button is clicked
	 * 
	 * @author ricky barrette
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ringer_info_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Called when the ringer info has changed (non-Javadoc)
	 * 
	 * @see org.RickBarrette.android.LocationRinger.OnContentChangedListener#onInfoContentChanged(android.content.ContentValues)
	 */
	@Override
	public void onInfoContentChanged(final ContentValues values) {
		if(Constraints.VERBOSE) {
			Log.v(TAG, "onInfoContentChanged()");
			logContentValues(values);
		}
		mInfo.putAll(values);
	}

	/**
	 * Called when a feature is removed (non-Javadoc)
	 * 
	 * @see org.RickBarrette.android.LocationRinger.OnContentChangedListener#onInfoContentRemoved(java.lang.String[])
	 */
	@Override
	public void onInfoContentRemoved(final String... keys) {
		for (final String key : keys)
			if (mInfo.containsKey(key))
				mInfo.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * @author ricky barrette
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			save();
			break;
		case android.R.id.home:
			final Intent intent = new Intent(this, ListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub
	}

	/**
	 * called when the pager's page is changed we use this to dismiss the soft
	 * keyboard (non-Javadoc)
	 * 
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 */
	@Override
	public void onPageScrollStateChanged(final int arg0) {
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mPager.getWindowToken(), 0);
	}

	@Override
	public void onPageSelected(final int arg0) {
	}

	/**
	 * Called when the ringer content has been changed (non-Javadoc)
	 * 
	 * @see org.RickBarrette.android.LocationRinger.OnContentChangedListener#onRingerContentChanged(android.content.ContentValues)
	 */
	@Override
	public void onRingerContentChanged(final ContentValues values) {
		if(Constraints.VERBOSE) {
			Log.v(TAG, "onRingerContentChanged()");
			logContentValues(values);
		}
		mRinger.putAll(values);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSearchRequested()
	 */
	@Override
	public boolean onSearchRequested() {
		if (mLocationInfomationFragment != null && mPager.getCurrentItem() == 1)
			return mLocationInfomationFragment.onSearchRequested();
		return super.onSearchRequested();
	}

	/**
	 * Prepares a bundle containing all the information that needs to be saved,
	 * and returns it to the starting activity
	 * 
	 * @author ricky barrette
	 */
	private void save() {
		final ProgressDialog progress = ProgressDialog.show(this, "", getText(R.string.saving), true, true);

		// Generate the intent in a thread to prevent anr's and allow for
		// progress dialog
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				mData.putExtra(ListActivity.KEY_RINGER, mRinger).putExtra(ListActivity.KEY_INFO, mInfo);
				RingerInformationActivity.this.setResult(Activity.RESULT_OK, mData);
				progress.dismiss();
				RingerInformationActivity.this.finish();
			}
		}).start();
	}

	/**
	 * Called when the scrolling state of the view pager is changed
	 * (non-Javadoc)
	 * 
	 * @see org.RickBarrette.android.LocationRinger.EnableScrollingListener#setScrollEnabled(boolean)
	 */
	@Override
	public void setScrollEnabled(final boolean enabled) {
		mPager.setScrollEnabled(enabled);
	}

	/**
	 * Handles results from activities. checks for a call back
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if(mFragmentCallBack != null) {
			mFragmentCallBack.onActivityResult(requestCode, resultCode, data);
			mFragmentCallBack = null;
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Sets up a call back for a fragment
	 * @param fragmentCallBack
	 */
	public void setFragmentCallBack(RingtoneFragment fragmentCallBack) {
		mFragmentCallBack = fragmentCallBack;
	}
}