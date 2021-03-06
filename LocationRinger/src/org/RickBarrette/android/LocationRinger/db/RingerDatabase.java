/**
 * RingerDatabase.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.RickBarrette.android.LocationRinger.Constraints;
import org.RickBarrette.android.LocationRinger.Log;
import org.RickBarrette.android.LocationRinger.R;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * This class will be the main interface between location ringer and it's
 * database
 * 
 * @author ricky barrette
 */
public class RingerDatabase {

	/**
	 * A helper class to manage database creation and version management.
	 * 
	 * @author ricky barrette
	 */
	private class OpenHelper extends SQLiteOpenHelper {

		/**
		 * Creates a new OpenHelper
		 * 
		 * @param context
		 * @author ricky barrette
		 */
		public OpenHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Converts the database from version 2 to 3
		 * 
		 * @param db
		 * @author ricky barrette
		 */
		private void convert2to3(final SQLiteDatabase db) {
			// get all the ringer information from the old table
			final Cursor cursor = db.query("two", new String[] { KEY_RINGER_NAME, KEY_RINGTONE, KEY_NOTIFICATION_RINGTONE, KEY_RINGTONE_IS_SILENT,
					KEY_NOTIFICATION_IS_SILENT, KEY_IS_ENABLED, KEY_RADIUS, KEY_LOCATION_LAT, KEY_LOCATION_LON, KEY_RINGTONE_URI, KEY_NOTIFICATION_RINGTONE_URI,
					KEY_RINGTONE_VOLUME, KEY_NOTIFICATION_RINGTONE_VOLUME, KEY_WIFI, KEY_BT, KEY_MUSIC_VOLUME, KEY_ALARM_VOLUME }, null, null, null, null, null);

			/*
			 * iterate through the database moving data over to the version 3
			 * tables
			 */
			final int count = cursor.getColumnCount();
			if (cursor.moveToFirst())
				do {
					final ContentValues ringer = new ContentValues();
					if(Constraints.VERBOSE)
						Log.v(TAG, "Converting: " + cursor.getString(0));
					for (int i = 0; i < count; i++) {
						if(Constraints.VERBOSE)
							Log.v(TAG, i + " = " + cursor.getColumnName(i) + " ~ " + cursor.getString(i));
						switch (i) {
						case 0: // ringer name
							ringer.put(cursor.getColumnName(i), cursor.getString(0));
							break;
						case 5: // is enabled
							ringer.put(KEY_IS_ENABLED, cursor.getString(i));
							break;
						case 6: // radius
							ringer.put(KEY_RADIUS, cursor.getString(i));
							break;
						case 7: // lat
							ringer.put(KEY_LOCATION_LAT, cursor.getString(i));
							break;
						case 8: // lon
							ringer.put(KEY_LOCATION_LON, cursor.getString(i));
							break;
						default:
							final ContentValues values = new ContentValues();
							values.put(KEY_RINGER_NAME, cursor.getString(0));
							values.put(KEY, cursor.getColumnName(i));
							values.put(KEY_VALUE, cursor.getString(i));
							db.insert(RINGER_INFO_TABLE, null, values);
						}
					}
					db.insert(RINGER_TABLE, null, ringer);
				} while (cursor.moveToNext());
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}

		/**
		 * Creates the initial database structure
		 * 
		 * @param db
		 * @author ricky barrette
		 */
		private void createDatabase(final SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + RINGER_TABLE + "(id INTEGER PRIMARY KEY, " + KEY_RINGER_NAME + " TEXT, " + KEY_IS_ENABLED + " TEXT)");
			db.execSQL("CREATE TABLE " + RINGER_INFO_TABLE + "(id INTEGER PRIMARY KEY, " + KEY_RINGER_NAME + " TEXT, " + KEY + " TEXT, " + KEY_VALUE + " TEXT)");
		}

		/**
		 * called when the database is created for the first time. this will
		 * create our Ringer database (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
		 * @author ricky barrette
		 */
		@Override
		public void onCreate(final SQLiteDatabase db) {
			if (Constraints.DROP_TABLE_EVERY_TIME)
				db.execSQL("DROP TABLE IF EXISTS " + RINGER_TABLE);
			createDatabase(db);
			// insert the default ringer into this table
			db.execSQL("insert into " + RINGER_TABLE + "(" + KEY_RINGER_NAME + ") values ('" + mContext.getString(R.string.default_ringer) + "')");
			db.execSQL("insert into " + RINGER_INFO_TABLE + "(" + KEY_RINGER_NAME + ", " + KEY + ", " + KEY_VALUE + ") values ('"
					+ mContext.getString(R.string.default_ringer) + "', '" + KEY_RINGER_DESCRIPTION + "', '" + mContext.getString(R.string.about_default_ringer) + "')");
			if (mListener != null)
				mListener.onDatabaseCreate();
		}

		/**
		 * called when the database needs to be updated (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
		 *      int, int)
		 * @author ricky barrette
		 */
		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			if(Constraints.INFO)
				Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

			if (mListener != null)
				mListener.onDatabaseUpgrade();

			isUpgrading = true;

			// upgrade thread
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					switch (oldVersion) {
					case 1:
						db.execSQL("ALTER TABLE " + RINGER_TABLE + " ADD " + KEY_MUSIC_VOLUME + " INTEGER");
						db.execSQL("ALTER TABLE " + RINGER_TABLE + " ADD " + KEY_ALARM_VOLUME + " INTEGER");
					case 2:
						// rename the old ringer table
						db.execSQL("ALTER TABLE " + RINGER_TABLE + " RENAME TO two");
						// create a new ringer table
						createDatabase(db);
						// convert database to the new version
						convert2to3(db);
						// remove old tables
						db.execSQL("DROP TABLE IF EXISTS two");
					case 3:
						final Cursor c = db.query(RINGER_TABLE, new String[] { "id", KEY_RINGER_NAME, KEY_LOCATION_LAT, KEY_LOCATION_LON, KEY_RADIUS }, null, null, null,
								null, null);
						;
						c.moveToFirst();
						if (c.moveToFirst())
							do {
								if(Constraints.VERBOSE)
									Log.v(TAG, "Moving: " + c.getInt(0) + " " + c.getString(1) + " " + c.getInt(2) + ", " + c.getInt(3) + " @ " + c.getInt(4) + "m");
								final ContentValues ringer = new ContentValues();
								final ContentValues info = new ContentValues();
								ringer.put(KEY_RINGER_NAME, c.getString(1));
								info.put(KEY_LOCATION_LAT, c.getInt(2));
								info.put(KEY_LOCATION_LON, c.getInt(3));
								info.put(KEY_RADIUS, c.getInt(4));
								updateRinger(c.getInt(0), ringer, info);
							} while (c.moveToNext());
						// drop old location trigger information
						db.execSQL("CREATE TABLE ringers_new (" + "id INTEGER PRIMARY KEY, " + KEY_RINGER_NAME + " TEXT, " + KEY_IS_ENABLED + " TEXT)");
						db.execSQL("INSERT INTO ringers_new SELECT id, " + KEY_RINGER_NAME + ", " + KEY_IS_ENABLED + " FROM " + RINGER_TABLE);
						db.execSQL("DROP TABLE " + RINGER_TABLE);
						db.execSQL("ALTER TABLE ringers_new RENAME TO " + RINGER_TABLE);

					}
					mHandler.sendEmptyMessage(DATABASEUPGRADECOMPLETE);
					isUpgrading = false;
				}
			}).start();
		}
	}

	private static final String TAG = "RingerDatabase";
	private final Context mContext;
	private SQLiteDatabase mDb;
	private static DatabaseListener mListener;
	public boolean isUpgrading = false;
	private final static Handler mHandler;
	protected static final int DELETIONCOMPLETE = 0;

	protected static final int DATABASEUPGRADECOMPLETE = 1;

	static {
		mHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				if (mListener != null)
					switch (msg.what) {
					case DELETIONCOMPLETE:
						mListener.onRingerDeletionComplete();
						break;
					case DATABASEUPGRADECOMPLETE:
						mListener.onDatabaseUpgradeComplete();
						break;
					}
			}
		};
	}

	/*
	 * database information values
	 */
	private final int DATABASE_VERSION = 4;
	/*
	 * the following is for the table that holds the other table names
	 */
	private final String DATABASE_NAME = "ringers.db";
	private final String RINGER_TABLE = "ringers";

	private static final String RINGER_INFO_TABLE = "ringer_info";
	/*
	 * Database keys
	 */
	@Deprecated
	public final static String KEY_LOCATION_LAT = "location_lat";
	@Deprecated
	public final static String KEY_LOCATION_LON = "location_lon";
	@Deprecated
	public final static String KEY_RINGTONE = "home_ringtone";
	@Deprecated
	public final static String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
	@Deprecated
	public final static String KEY_RINGTONE_IS_SILENT = "ringtone_is_silent";
	@Deprecated
	public final static String KEY_NOTIFICATION_IS_SILENT = "notification_is_silent";

	public final static String KEY_IS_ENABLED = "is_enabled";
	public final static String KEY_RADIUS = "radius";
	public final static String KEY_RINGER_NAME = "ringer_name";
	public final static String KEY_LOCATION = "location";
	public final static String KEY_RINGTONE_URI = "ringtone_uri";
	public final static String KEY_NOTIFICATION_RINGTONE_URI = "away_notification_uri";
	public final static String KEY_RINGTONE_VOLUME = "ringtone_volume";
	public final static String KEY_NOTIFICATION_RINGTONE_VOLUME = "notification_ringtone_volume";
	public final static String KEY_WIFI = "wifi";
	public final static String KEY_BT = "bt";
	public final static String KEY_MUSIC_VOLUME = "music_volume";
	public final static String KEY_ALARM_VOLUME = "alarm_volume";
	public static final String KEY_VALUE = "value";
	public static final String KEY = "key";
	public static final String KEY_UPDATE_INTERVAL = "update_interval";
	public static final String KEY_PLUS_BUTTON_HINT = "plus_button_hint";
	public static final String KEY_DTMF_VOLUME = "dtmf_volume";
	public static final String KEY_SYSTEM_VOLUME = "system_volume";
	public static final String KEY_CALL_VOLUME = "call_volume";
	public static final String KEY_RINGER_DESCRIPTION = "ringer_description";
	public static final String KEY_AIRPLANE_MODE = "AIRPLANE_MODE";

	/**
	 * Parses a string boolean from the database
	 * 
	 * @param bool
	 * @return true or false
	 * @author ricky barrette
	 */
	public static boolean parseBoolean(final String bool) {
		try {
			return bool == null ? false : Integer.parseInt(bool) == 1 ? true : false;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Creates a new RingerDatabase
	 * 
	 * @param context
	 * @author ricky barrette
	 */
	public RingerDatabase(final Context context) {
		this(context, null);
	}

	/**
	 * Creates a new RingerDatabase
	 * 
	 * @param context
	 * @param listener
	 * @author ricky barrette
	 */
	public RingerDatabase(final Context context, final DatabaseListener listener) {
		mListener = listener;
		mContext = context;
		mDb = new OpenHelper(mContext).getWritableDatabase();
	}

	/**
	 * Backs up the database
	 * 
	 * @return true if successful
	 * @author ricky barrette
	 */
	public boolean backup() {
		final File dbFile = new File(Environment.getDataDirectory() + "/data/" + mContext.getPackageName() + "/databases/" + DATABASE_NAME);

		final File exportDir = new File(Environment.getExternalStorageDirectory(), "/" + mContext.getString(R.string.app_name));
		if (!exportDir.exists())
			exportDir.mkdirs();

		final File file = new File(exportDir, dbFile.getName());

		try {
			file.createNewFile();
			copyFile(dbFile, file);
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks to see if this ringer name is original, if not it renames it
	 * 
	 * @param name
	 * @return
	 */
	private String checkRingerName(final String name) {

		final List<String> names = getAllRingerTitles();
		String ringerName = name;
		int count = 1;

		for (int index = 0; index < names.size(); index++)
			if (ringerName.equals(names.get(index))) {
				ringerName = name + count++ + "";
				index = 0;
			}
		return ringerName;
	}

	/**
	 * Copies a file
	 * 
	 * @param src
	 *            file
	 * @param dst
	 *            file
	 * @throws IOException
	 * @author ricky barrette
	 */
	private void copyFile(final File src, final File dst) throws IOException {
		final FileInputStream in = new FileInputStream(src);
		final FileOutputStream out = new FileOutputStream(dst);
		final FileChannel inChannel = in.getChannel();
		final FileChannel outChannel = out.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
			if (in != null)
				in.close();
			if (out != null)
				out.close();

		}
	}

	/**
	 * deletes a note by its row id
	 * 
	 * @param id
	 * @author ricky barrette
	 */
	public void deleteRinger(final long id) {

		final ProgressDialog progress = ProgressDialog.show(RingerDatabase.this.mContext, "", RingerDatabase.this.mContext.getText(R.string.deleteing), true, true);

		// ringer deleting thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();

				/*
				 * get the ringer name from the id, and then delete all its
				 * information from the ringer information table
				 */
				mDb.delete(RINGER_INFO_TABLE, KEY_RINGER_NAME + " = " + DatabaseUtils.sqlEscapeString(RingerDatabase.this.getRingerName(id)), null);

				/*
				 * finally delete the ringer from the ringer table
				 */
				mDb.delete(RINGER_TABLE, "id = " + id, null);
				updateRowIds(id + 1);
				mHandler.sendEmptyMessage(DELETIONCOMPLETE);
				progress.dismiss();
			}
		}).start();
	}

	/**
	 * returns all ringer descriptions in the database, where or not if they are
	 * enabled
	 * 
	 * @return list of all strings in the database table
	 * @author ricky barrette
	 */
	public List<String> getAllRingerDescriptions() {
		final List<String> list = new ArrayList<String>();
		final List<String> ringers = getAllRingerTitles();
		for (final String ringer : ringers)
			list.add(getRingerInfo(ringer).getAsString(KEY_RINGER_DESCRIPTION));
		return list;
	}

	/**
	 * @return a cursor containing all ringers
	 * @author ricky barrette
	 */
	public Cursor getAllRingers() {
		return mDb.query(RINGER_TABLE, new String[] { KEY_RINGER_NAME, KEY_IS_ENABLED }, null, null, null, null, null);
	}

	/**
	 * returns all ringer names in the database, where or not if they are
	 * enabled
	 * 
	 * @return list of all strings in the database table
	 * @author ricky barrette
	 */
	public List<String> getAllRingerTitles() {
		final List<String> list = new ArrayList<String>();
		final Cursor cursor = mDb.query(RINGER_TABLE, new String[] { KEY_RINGER_NAME }, null, null, null, null, null);
		if (cursor.moveToFirst())
			do
				list.add(cursor.getString(0));
			while (cursor.moveToNext());
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return list;
	}

	/**
	 * gets a ringer from a row id;
	 * 
	 * @param id
	 * @return cursor containing the note
	 * @author ricky barrette
	 */
	public Cursor getRingerFromId(final long id) {
		return mDb.query(RINGER_TABLE, new String[] { KEY_RINGER_NAME, KEY_IS_ENABLED }, "id = " + id, null, null, null, null);
	}

	/**
	 * gets a ringer's info from the supplied ringer name
	 * 
	 * @param ringerName
	 * @return
	 * @author ricky barrette
	 */
	public ContentValues getRingerInfo(final String ringerName) {
		final ContentValues values = new ContentValues();
		final Cursor info = mDb.query(RINGER_INFO_TABLE, new String[] { KEY, KEY_VALUE }, KEY_RINGER_NAME + " = " + DatabaseUtils.sqlEscapeString(ringerName), null,
				null, null, null);
		if (info.moveToFirst())
			do
				values.put(info.getString(0), info.getString(1));
			while (info.moveToNext());
		if (info != null && !info.isClosed())
			info.close();
		return values;
	}

	/**
	 * Retrieves the ringer's name form the ringer table
	 * 
	 * @param id
	 * @return ringer's name
	 * @author ricky barrette
	 */
	public String getRingerName(final long id) {
		String name = null;
		final Cursor cursor = mDb.query(RINGER_TABLE, new String[] { KEY_RINGER_NAME }, "id = " + id, null, null, null, null);
		;
		if (cursor.moveToFirst())
			name = cursor.getString(0);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		return name;
	}

	/**
	 * Inserts a new ringer into the database
	 * 
	 * @param ringer
	 *            values
	 * @param ringerInfo
	 *            values
	 * @author ricky barrette
	 */
	public void insertRinger(final ContentValues ringer, final ContentValues ringerInfo) {
		ringer.put(RingerDatabase.KEY_RINGER_NAME, checkRingerName(ringer.getAsString(RingerDatabase.KEY_RINGER_NAME)));
		mDb.insert(RINGER_TABLE, null, ringer);
		final String ringerName = ringer.getAsString(RingerDatabase.KEY_RINGER_NAME);

		// insert the information values
		for (final Entry<String, Object> item : ringerInfo.valueSet()) {
			final ContentValues values = new ContentValues();
			values.put(KEY_RINGER_NAME, ringerName);
			values.put(KEY, item.getKey());
			/*
			 * Try get the value. If there is a class cast exception, try
			 * casting to the next object type.
			 * 
			 * The following types are tried: String Integer Boolean
			 */
			try {
				values.put(KEY_VALUE, (String) item.getValue());
			} catch (final ClassCastException e) {
				try {
					values.put(KEY_VALUE, (Boolean) item.getValue() ? 1 : 0);
				} catch (final ClassCastException e1) {
					values.put(KEY_VALUE, (Integer) item.getValue());
				}
			}
			mDb.insert(RINGER_INFO_TABLE, null, values);
		}
	}

	/**
	 * Checks to see if a ringer is enabled
	 * 
	 * @param row
	 *            id
	 * @return true if the ringer is enabled
	 * @author ricky barrette
	 */
	public boolean isRingerEnabled(final long id) {
		final Cursor cursor = mDb.query(RINGER_TABLE, new String[] { KEY_IS_ENABLED }, "id = " + id, null, null, null, null);
		if (cursor.moveToFirst()) {
			if(Constraints.VERBOSE)
				Log.v(TAG, "isRingerEnabled(" + id + ") = " + cursor.getString(0));
			return parseBoolean(cursor.getString(0));
		}
		return false;
	}

	/**
	 * Restores the database from the sdcard
	 * 
	 * @return true if successful
	 * @author ricky barrette
	 */
	public void restore() {
		final File dbFile = new File(Environment.getDataDirectory() + "/data/" + mContext.getPackageName() + "/databases/" + DATABASE_NAME);

		final File exportDir = new File(Environment.getExternalStorageDirectory(), "/" + mContext.getString(R.string.app_name));
		if (!exportDir.exists())
			exportDir.mkdirs();

		final File file = new File(exportDir, dbFile.getName());

		try {
			file.createNewFile();
			copyFile(file, dbFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		/*
		 * close and reopen the database to upgrade it.
		 */
		mDb.close();
		mDb = new OpenHelper(mContext).getWritableDatabase();
		if (mDb.isOpen() && !isUpgrading)
			if (mListener != null)
				mListener.onRestoreComplete();
	}

	public int setRingerEnabled(final long id, final boolean enabled) {
		if(Constraints.VERBOSE)
			Log.v(TAG, "setRingerEnabled(" + id + ") = " + enabled);
		final ContentValues values = new ContentValues();
		values.put(KEY_IS_ENABLED, enabled);
		return mDb.update(RINGER_TABLE, values, "id" + "= " + id, null);
	}

	/**
	 * updates a ringer by it's id
	 * 
	 * @param id
	 * @param ringer
	 *            values
	 * @param info
	 *            values
	 * @author ricky barrette
	 */
	public void updateRinger(final long id, final ContentValues ringer, final ContentValues info) throws NullPointerException {

		if (ringer == null || info == null)
			throw new NullPointerException("ringer content was null");

		final String ringer_name = getRingerName(id);

		/*
		 * here we retrive the old values. we will compare the old value against
		 * the new values. we will delete all values that are NOT included in
		 * the new values.
		 */
		final ContentValues old = getRingerInfo(ringer_name);
		for (final Entry<String, Object> item : info.valueSet())
			if (old.containsKey(item.getKey()))
				old.remove(item.getKey());
		for (final Entry<String, Object> item : old.valueSet())
			RingerDatabase.this.mDb.delete(RINGER_INFO_TABLE, KEY + " = " + DatabaseUtils.sqlEscapeString(item.getKey()) + " and " + KEY_RINGER_NAME + " = "
					+ DatabaseUtils.sqlEscapeString(ringer_name), null);

		/*
		 * here we want to update the ringer name if needed
		 */
		if (!ringer_name.equals(ringer.getAsString(RingerDatabase.KEY_RINGER_NAME)))
			ringer.put(RingerDatabase.KEY_RINGER_NAME, checkRingerName(ringer.getAsString(RingerDatabase.KEY_RINGER_NAME)));

		/*
		 * here we wanr to update the information values in the info table
		 */
		for (final Entry<String, Object> item : info.valueSet()) {
			final ContentValues values = new ContentValues();
			values.put(KEY_RINGER_NAME, ringer.getAsString(KEY_RINGER_NAME));
			values.put(KEY, item.getKey());
			try {
				values.put(KEY_VALUE, (String) item.getValue());
			} catch (final ClassCastException e) {
				try {
					values.put(KEY_VALUE, (Boolean) item.getValue() ? 1 : 0);
				} catch (final ClassCastException e1) {
					values.put(KEY_VALUE, (Integer) item.getValue());
				}
			}

			/*
			 * here we are going to try to update a row, if that fails we will
			 * insert a row insead
			 */
			if (!(mDb.update(RINGER_INFO_TABLE, values, KEY_RINGER_NAME + "=" + DatabaseUtils.sqlEscapeString(ringer_name) + " AND " + KEY + "='" + item.getKey() + "'",
					null) > 0))
				mDb.insert(RINGER_INFO_TABLE, null, values);
		}

		// update the ringer table
		mDb.update(RINGER_TABLE, ringer, "id" + "= " + id, null);
	}

	/**
	 * Updates the row ids after a row is deleted
	 * 
	 * @param id
	 *            of the row to start with
	 * @author ricky barrette
	 */
	private void updateRowIds(long id) {
		long currentRow;
		final ContentValues values = new ContentValues();
		final Cursor cursor = mDb.query(RINGER_TABLE, new String[] { "id" }, null, null, null, null, null);
		if (cursor.moveToFirst())
			do {
				currentRow = cursor.getLong(0);
				if (currentRow == id) {
					id++;
					values.clear();
					values.put("id", currentRow - 1);
					mDb.update(RINGER_TABLE, values, "id" + "= " + currentRow, null);
				}
			} while (cursor.moveToNext());
		if (cursor != null && !cursor.isClosed())
			cursor.close();
	}
}