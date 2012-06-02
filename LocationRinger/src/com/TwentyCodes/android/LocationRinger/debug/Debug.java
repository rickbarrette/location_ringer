/**
 * Debug.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.debug;

import android.content.Context;

/**
 * A convince class containing debugging variables
 * @author ricky barrette */
public class Debug {
	
	public static final boolean SUPPORTS_FROYO;

	public static final boolean SUPPORTS_GINGERBREAD;

	public static final boolean SUPPORTS_HONEYCOMB;
	
	public static final int SHARED_PREFS_MODE;

	static{
		SUPPORTS_FROYO = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
		
		SUPPORTS_GINGERBREAD = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
		
		SUPPORTS_HONEYCOMB = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
		
		SHARED_PREFS_MODE = SUPPORTS_HONEYCOMB ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE;
	}
	
	/**
	 * Sets the logging output of this application
	 */
	public static final boolean DEBUG = true;

	/**
	 * The amount of intersecting that is needed between a users accuracy radius and a ringers location radius
	 */
	public static final float FUDGE_FACTOR = .002f;
	
	/**
	 * Drops the ringer database table every time the database is created
	 */
	public static boolean DROP_TABLE_EVERY_TIME = false;
	
	/**
	 * Max radius that can be set by a ringer
	 */
	public static final int MAX_RADIUS_IN_METERS = 600;
}