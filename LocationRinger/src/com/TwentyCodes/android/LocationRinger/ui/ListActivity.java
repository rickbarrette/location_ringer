/**
 * RingerListActivity.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */

package com.TwentyCodes.android.LocationRinger.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.db.DatabaseListener;
import com.TwentyCodes.android.LocationRinger.db.RingerDatabase;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.receivers.PassiveLocationChangedReceiver;
import com.TwentyCodes.android.LocationRinger.services.LocationService;
import com.TwentyCodes.android.SkyHook.SkyHookRegistration;
import com.TwentyCodes.android.debug.LocationLibraryConstants;
import com.TwentyCodes.android.location.PassiveLocationListener;
import com.skyhookwireless.wps.RegistrationCallback;
import com.skyhookwireless.wps.WPSContinuation;
import com.skyhookwireless.wps.WPSReturnCode;

@SuppressLint("Registered")
public class ListActivity extends Activity implements OnItemClickListener, OnClickListener, DatabaseListener, RegistrationCallback {
	
    private RingerDatabase mDb;
	private ListView mListView;
	private SharedPreferences mSettings;
	private ProgressDialog mProgress;
	private Dialog mSplashDialog;
	
	public static final String NO_SPLASH = "no splash";
	public static final String KEY_RINGER = "key_ringer";
	public static final String KEY_INFO = "key_info";
	public static final String KEY_IS_DEFAULT = "key_is_default";
	private static final int ACTIVITY_CREATE = 3;
	private static final int ACTIVITY_EDIT = 4;
	private static final String KEY_ROWID = "key_row_id";
	
	@Override
	public void done() {
	}

	@Override
	public WPSContinuation handleError(WPSReturnCode arg0) {
		Toast.makeText(this, R.string.skyhook_error_registration, Toast.LENGTH_SHORT).show();
		return WPSContinuation.WPS_CONTINUE;
	}
    
    @Override
	public void handleSuccess() {
		Toast.makeText(this, R.string.registered, Toast.LENGTH_SHORT).show();
		this.mSettings.edit().putBoolean(SettingsActivity.IS_REGISTERED, true).commit();
	}
    
    /**
	 * called when the note edit activity finishes
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * @author ricky barrette
	 */
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK){
			
			final ProgressDialog progress = ProgressDialog.show(this, "", this.getText(R.string.saving), true, true);
		
			new Thread(new Runnable() {
				@Override
				public void run() {
					switch (requestCode) {
					case ACTIVITY_CREATE:
						ContentValues ringer = (ContentValues) intent.getParcelableExtra(KEY_RINGER);
						mDb.insertRinger(ringer, (ContentValues) intent.getParcelableExtra(KEY_INFO));
						break;
					case ACTIVITY_EDIT:
						mDb.updateRinger(intent.getLongExtra(KEY_ROWID, 1), (ContentValues) intent.getParcelableExtra(KEY_RINGER), (ContentValues) intent.getParcelableExtra(KEY_INFO));
						break;
					}
					
					ListActivity.this.mListView.post(new Runnable(){
						@Override
						public void run(){
							progress.dismiss();
							populate();
						}
					});
				}
			}).start();
		}
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(this, RingerInformationActivity.class);
		startActivityForResult(i, ACTIVITY_CREATE );		
	}

	/**
     * called when the context menu item has been selected
     * (non-Javadoc)
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     * @author ricky barrette
     */
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        	case R.id.delete:
        		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        		if(info.id == 0)
        			Toast.makeText(this, this.getString(R.string.cant_delete_default), Toast.LENGTH_SHORT).show();
        		else
        			mDb.deleteRinger(info.id +1);
                return true;
        }
        return super.onContextItemSelected(item);
    }

	/** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ringer_list);
        this.setTitle(R.string.app_name);
        this.mDb = new RingerDatabase(this, this);
        this.mListView = (ListView) findViewById(R.id.ringer_list);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setOnCreateContextMenuListener(this);
        this.mListView.setEmptyView(findViewById(android.R.id.empty));
        findViewById(R.id.add_ringer_button).setOnClickListener(this);
        populate();
        this.mSettings = this.getSharedPreferences(SettingsActivity.SETTINGS, Debug.SHARED_PREFS_MODE);
        
        if(this.mSettings.getBoolean(SettingsActivity.IS_FIRST_BOOT, true))
        	new FirstBootDialog(this).show();
        
        if(! this.mSettings.getBoolean(SettingsActivity.IS_REGISTERED, false)){
        	new SkyHookRegistration(this).registerNewUser(this);
        }
        
        if(!this.getIntent().hasExtra(NO_SPLASH))
        	showSplashScreen();
    }

	/**
     * called when the activity is first created, creates a context menu
     * @param menu
     * @param v
     * @param menuInfo
     * @return
     * @author ricky barrette
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ringer_list_context_menu, menu);
    }
    
    /**
     * called when the activity is first created, creates options menu
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     * @author ricky barrette
     */
    @Override
	public boolean onCreateOptionsMenu (Menu menu) {
    	final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ringer_list_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    	
    }
	
	/**
     * Called when a database is being upgraded
     * @author ricky barrette
     */
    @Override
	public void onDatabaseUpgrade() {
		this.mProgress = ProgressDialog.show(this, "", this.getText(R.string.upgrading), true, true);
	}
    
    /**
     * called when a database upgrade is finished
     * @author ricky barrette
     */
	@Override
	public void onDatabaseUpgradeComplete() {
		populate();
		if(this.mProgress != null)
			this.mProgress.dismiss();
	}
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 * @author ricky barrette
	 */
	@Override
	protected void onDestroy() {
		restartService();
		PassiveLocationListener.requestPassiveLocationUpdates(this, new Intent(this, PassiveLocationChangedReceiver.class));
		super.onDestroy();
	}
    
    /**
     * called when an item in the list view has been clicked, 
     * this will open the note edit dialog for the selected note
     * (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     * @author ricky barrette
     */
    @Override
	public void onItemClick(AdapterView<?> arg0, View v, int postion, final long id) {
    	
    	final ProgressDialog progress = ProgressDialog.show(this, "", this.getText(R.string.loading), true, true);
    	
    	//post to social sites in a new thread to prevent ANRs
		 new Thread( new Runnable(){
			 @Override
			 public void run(){
				 Looper.prepare();
				 
		    	final Intent i = new Intent(ListActivity.this, RingerInformationActivity.class)
		    	.putExtra(KEY_ROWID, id+1);
		    	
		    	/*
		    	 * get the ringer
		    	 */
		    	final Cursor ringer = mDb.getRingerFromId(id+1);
		    	if (ringer.moveToFirst()){
		    		ContentValues r = new ContentValues();
		    		r.put(RingerDatabase.KEY_RINGER_NAME, ringer.getString(0));
		    		r.put(RingerDatabase.KEY_IS_ENABLED, RingerDatabase.parseBoolean(ringer.getString(1)));
		    		i.putExtra(KEY_RINGER, r);
		    	
					if (ringer != null && !ringer.isClosed()) {
						ringer.close();
					}
					
					if(id == 0)
						i.putExtra(KEY_IS_DEFAULT, true);
			    	
			    	/*
			    	 * get the ringer's info, and parse it into content values
			    	 */
					i.putExtra(KEY_INFO, mDb.getRingerInfo(r.getAsString(RingerDatabase.KEY_RINGER_NAME)));
		    	}
				
				progress.dismiss();
				
				//start the ringer info activity in editor mode
		    	startActivityForResult(i, ACTIVITY_EDIT);

			 }
		 }).start();
	}

    /**
     * called when an option is selected form the menu
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     * @author ricky barrette
     */
    @Override
	public boolean onOptionsItemSelected (MenuItem item) {
    	switch (item.getItemId()){
    		case R.id.settings:
    			startActivity(new Intent(this, SettingsActivity.class));
    			return true;
    			
    		case R.id.backup:
    			mDb.backup();
    			SettingsActivity.backup(this);
    			break;
    			
    		case R.id.restore:
    			mDb.restore();
    			SettingsActivity.restore(this);
    			break;
    	}
    	return super.onOptionsItemSelected(item);
    }

	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		removeSplashScreen();
		super.onPause();
	}

	/**
     * Called when the database is restored
     */
	@Override
	public void onRestoreComplete() {
		populate();		
	}

	/**
	 * called when a ringer is deleted
	 */
	@Override
	public void onRingerDeletionComplete() {
		populate();		
	}

    /**
	 * populates the list view from the data base
	 * @author ricky barrette
	 */
	private void populate() {
		findViewById(R.id.add_ringer_button_hint).setVisibility(this.mDb.getAllRingerTitles().size() > 1 ? View.GONE : View.VISIBLE);
		mListView.setAdapter(new RingerListAdapter(this, mDb));
	}

	/**
	 * Removes the Dialog that displays the splash screen
	 */
	protected void removeSplashScreen() {
	    if (mSplashDialog != null) {
	        mSplashDialog.dismiss();
	        mSplashDialog = null;
	    }
	}

	/**
     * Restarts the service if its not already running.
     * @author ricky barrette
     */
    private void restartService() {
    	final SharedPreferences sharedPrefs = this.getSharedPreferences(SettingsActivity.SETTINGS, Debug.SHARED_PREFS_MODE);
		if(! sharedPrefs.getBoolean(SettingsActivity.IS_SERVICE_STARTED, false)){
			// cancel the previous service
			LocationService.stopService(this).run();
			//start the new service
			Intent i = new Intent(this, LocationService.class)
			.putExtra(LocationService.INTENT_EXTRA_REQUIRED_ACCURACY, Integer.parseInt(sharedPrefs.getString(SettingsActivity.ACCURACY , "50")))
			.setAction(LocationLibraryConstants.INTENT_ACTION_UPDATE);
			this.startService(i);	
		}	
	}

	/**
	 * Shows the splash screen over the full Activity
	 */
	protected void showSplashScreen() {
//		mMap.setGPSDialogEnabled(false);
	    mSplashDialog = new Dialog(this, android.R.style.Theme_Translucent);
	    mSplashDialog.setContentView(R.layout.powered_by_skyhook);
	    mSplashDialog.setCancelable(false);
	    mSplashDialog.show();
	 
	    // Set Runnable to remove splash screen just in case
	    final Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
	      @Override
	      public void run() {
	        removeSplashScreen();
	        
	        /*
	         * uncomment the following to display the eula
	         */
//	      //loads first boot dialog if this is the first boot
//			if (! mSettings.getBoolean(Settings.ACCEPTED, false) || Debug.FORCE_FIRSTBOOT_DIALOG)
//				eulaAlert();
//			else
//				update();
	      }
	    }, 2000);
	}
}