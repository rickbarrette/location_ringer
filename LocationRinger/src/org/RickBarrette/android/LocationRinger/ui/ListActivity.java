/**
 * RingerListActivity.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */

package org.RickBarrette.android.LocationRinger.ui;

import org.RickBarrette.android.LocationRinger.Constraints;
import org.RickBarrette.android.LocationRinger.R;
import org.RickBarrette.android.LocationRinger.db.DatabaseListener;
import org.RickBarrette.android.LocationRinger.db.RingerDatabase;
import org.RickBarrette.android.LocationRinger.receivers.PassiveLocationChangedReceiver;
import org.RickBarrette.android.LocationRinger.services.LocationService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
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

import com.TwentyCodes.android.exception.ExceptionHandler;
import com.TwentyCodes.android.location.PassiveLocationListener;

@SuppressLint("Registered")
public class ListActivity extends Activity implements OnItemClickListener, OnClickListener, DatabaseListener {

	private RingerDatabase mDb;
	private ListView mListView;
	private SharedPreferences mSettings;
	private ProgressDialog mProgress;
	private Dialog mSplashDialog;

	public static final String KEY_RINGER = "key_ringer";
	public static final String KEY_INFO = "key_info";
	public static final String KEY_IS_DEFAULT = "key_is_default";
	private static final int ACTIVITY_CREATE = 3;
	private static final int ACTIVITY_EDIT = 4;
	private static final String KEY_ROWID = "key_row_id";
	public static final String ACTION_NEW_RINGER = "action_new_ringer";

	/**
	 * called when the note edit activity finishes (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 * @author ricky barrette
	 */
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (mProgress != null)
			mProgress.dismiss();

		if (resultCode == RESULT_OK) {

			final ProgressDialog progress = ProgressDialog.show(this, "", getText(R.string.saving), true, true);

			new Thread(new Runnable() {
				@Override
				public void run() {
					switch (requestCode) {
					case ACTIVITY_CREATE:
						final ContentValues ringer = (ContentValues) intent.getParcelableExtra(KEY_RINGER);
						mDb.insertRinger(ringer, (ContentValues) intent.getParcelableExtra(KEY_INFO));
						break;
					case ACTIVITY_EDIT:
						mDb.updateRinger(intent.getLongExtra(KEY_ROWID, 1), (ContentValues) intent.getParcelableExtra(KEY_RINGER),
								(ContentValues) intent.getParcelableExtra(KEY_INFO));
						break;
					}

					final String action = ListActivity.this.getIntent().getAction();
					if (action != null)
						if (action.equals(ACTION_NEW_RINGER))
							finish();
						else
							mListView.post(new Runnable() {
								@Override
								public void run() {
									populate();
									progress.dismiss();
								}
							});
				}
			}).start();
		} else {
			final String action = ListActivity.this.getIntent().getAction();
			if (action != null)
				if (action.equals(ACTION_NEW_RINGER))
					finish();
		}
	}

	@Override
	public void onClick(final View v) {
		mProgress = ProgressDialog.show(this, "", getText(R.string.preparing_ringer), true, true);
		final Intent i = new Intent(this, RingerInformationActivity.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	/**
	 * called when the context menu item has been selected (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 * @author ricky barrette
	 */
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete:
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			if (info.id == 0)
				Toast.makeText(this, this.getString(R.string.cant_delete_default), Toast.LENGTH_SHORT).show();
			else
				mDb.deleteRinger(info.id + 1);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

		final Intent intent = getIntent();
		final String action = intent.getAction();

		// If the intent is a request to create a shortcut, we'll do that and
		// exit

		if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
			setupShortcut();
			finish();
			return;
		}

		setContentView(R.layout.ringer_list);
		this.setTitle(R.string.app_name);
		mDb = new RingerDatabase(this, this);
		mListView = (ListView) findViewById(R.id.ringer_list);
		mListView.setOnItemClickListener(this);
		mListView.setOnCreateContextMenuListener(this);
		mListView.setEmptyView(findViewById(android.R.id.empty));
		findViewById(R.id.add_ringer_button).setOnClickListener(this);
		populate();
		mSettings = getSharedPreferences(SettingsActivity.SETTINGS, Constraints.SHARED_PREFS_MODE);

		if (mSettings.getBoolean(SettingsActivity.IS_FIRST_BOOT, true))
			new FirstBootDialog(this).show();

		if (action != null)
			if (action.equals(ACTION_NEW_RINGER))
				startActivityForResult(new Intent(this, RingerInformationActivity.class), ACTIVITY_CREATE);
	}

	/**
	 * called when the activity is first created, creates a context menu
	 * 
	 * @param menu
	 * @param v
	 * @param menuInfo
	 * @return
	 * @author ricky barrette
	 */
	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ringer_list_context_menu, menu);
	}

	/**
	 * called when the activity is first created, creates options menu
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * @author ricky barrette
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ringer_list_menu, menu);
		return super.onCreateOptionsMenu(menu);

	}

	/**
	 * Called when the database is first created. Here we want to populate the
	 * populate the default ringr
	 */
	@Override
	public void onDatabaseCreate() {
		final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		final WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		final BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		final ContentValues ringer = new ContentValues();
		final ContentValues info = new ContentValues();
		ringer.put(RingerDatabase.KEY_RINGER_NAME, getString(R.string.default_ringer));
		info.put(RingerDatabase.KEY_RINGER_DESCRIPTION, getString(R.string.about_default_ringer));
		info.put(RingerDatabase.KEY_ALARM_VOLUME, am.getStreamVolume(AudioManager.STREAM_ALARM));
		info.put(RingerDatabase.KEY_MUSIC_VOLUME, am.getStreamVolume(AudioManager.STREAM_MUSIC));
		Uri ringtoneURI = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
		info.put(RingerDatabase.KEY_NOTIFICATION_RINGTONE_URI, ringtoneURI != null ? ringtoneURI.toString() : null);
		info.put(RingerDatabase.KEY_NOTIFICATION_RINGTONE_VOLUME, am.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
		Uri notificationURI = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
		info.put(RingerDatabase.KEY_RINGTONE_URI, notificationURI != null ? notificationURI.toString() : null);
		info.put(RingerDatabase.KEY_RINGTONE_VOLUME, am.getStreamVolume(AudioManager.STREAM_RING));
		info.put(RingerDatabase.KEY_BT, bt.isEnabled());
		info.put(RingerDatabase.KEY_WIFI, wifi.isWifiEnabled());
		info.put(RingerDatabase.KEY_AIRPLANE_MODE, Settings.System.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0);

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				mDb.updateRinger(1, ringer, info);
			}
		});
	}

	/**
	 * Called when a database is being upgraded
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onDatabaseUpgrade() {
		mProgress = ProgressDialog.show(this, "", getText(R.string.upgrading), true, true);
	}

	/**
	 * called when a database upgrade is finished
	 * 
	 * @author ricky barrette
	 */
	@Override
	public void onDatabaseUpgradeComplete() {
		populate();
		if (mProgress != null)
			mProgress.dismiss();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 * 
	 * @author ricky barrette
	 */
	@Override
	protected void onDestroy() {
		restartService();
		PassiveLocationListener.requestPassiveLocationUpdates(this, new Intent(this, PassiveLocationChangedReceiver.class));
		super.onDestroy();
	}

	/**
	 * called when an item in the list view has been clicked, this will open the
	 * note edit dialog for the selected note (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 * @author ricky barrette
	 */
	@Override
	public void onItemClick(final AdapterView<?> arg0, final View v, final int postion, final long id) {

//		final ProgressDialog progress = ProgressDialog.show(this.getParent(), "", getText(R.string.loading), true, true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();

				final Intent i = new Intent(ListActivity.this, RingerInformationActivity.class).putExtra(KEY_ROWID, id + 1);

				/*
				 * get the ringer
				 */
				final Cursor ringer = mDb.getRingerFromId(id + 1);
				if (ringer.moveToFirst()) {
					final ContentValues r = new ContentValues();
					r.put(RingerDatabase.KEY_RINGER_NAME, ringer.getString(0));
					r.put(RingerDatabase.KEY_IS_ENABLED, RingerDatabase.parseBoolean(ringer.getString(1)));
					i.putExtra(KEY_RINGER, r);

					if (ringer != null && !ringer.isClosed())
						ringer.close();

					if (id == 0)
						i.putExtra(KEY_IS_DEFAULT, true);

					/*
					 * get the ringer's info, and parse it into content values
					 */
					i.putExtra(KEY_INFO, mDb.getRingerInfo(r.getAsString(RingerDatabase.KEY_RINGER_NAME)));
				}

//				progress.dismiss();

				// start the ringer info activity in editor mode
				startActivityForResult(i, ACTIVITY_EDIT);

			}
		}).start();
	}

	/**
	 * called when an option is selected form the menu (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * @author ricky barrette
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
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
	 * 
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
	 * 
	 * @author ricky barrette
	 */
	private void populate() {
		findViewById(R.id.add_ringer_button_hint).setVisibility(mDb.getAllRingerTitles().size() > 1 ? View.GONE : View.VISIBLE);
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
	 * 
	 * @author ricky barrette
	 */
	private void restartService() {
		final SharedPreferences sharedPrefs = getSharedPreferences(SettingsActivity.SETTINGS, Constraints.SHARED_PREFS_MODE);
		if (!sharedPrefs.getBoolean(SettingsActivity.IS_SERVICE_STARTED, false)) {
			// cancel the previous service
			com.TwentyCodes.android.location.LocationService.stopService(this).run();
			// start the new service
			LocationService.startMultiShotService(this);
		}
	}

	/**
	 * Creates a shortcut for the launcher
	 * 
	 * @author ricky barrette
	 */
	private void setupShortcut() {
		final Intent shortcutIntent = new Intent(this, this.getClass());
		shortcutIntent.setAction(ACTION_NEW_RINGER);

		// set up the container intent and return to the launcher
		final Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.new_ringer));
		final Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
		setResult(RESULT_OK, intent);
	}
}