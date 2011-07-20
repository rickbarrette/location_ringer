/**
 * Debug.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.debug;

/**
 * A convince class containing debugging variables
 * @author ricky barrette
 */
public class Debug {
	
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