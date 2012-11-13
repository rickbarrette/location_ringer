/**
 * Constraints.java
 * @date Sep 13, 2012
 * @author ricky barrette
 * 
 * Copyright 2012 Richard Barrette 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License
 */
package org.RickBarrette.android.LocationRinger;

import android.app.AlarmManager;
import android.content.Context;

/**
 * This class will be used to house the constraints of this application
 * 
 * @author ricky barrette
 */
public class Constraints {

	/**
	 * Set this boolean to true to use the test server
	 */
	public static final boolean TESTING = true;

	/**
	 * Set this boolean to true to enable debug logging
	 */
	public static final boolean DEBUG = true;

	/**
	 * Set this boolean to true to enable error logging
	 */
	public static final boolean ERROR = true;

	/**
	 * Set this boolean to true to enable info logging
	 */
	public static final boolean INFO = true;

	/**
	 * Set this boolean to true to enable verbose logging
	 */
	public static final boolean VERBOSE = true;

	/**
	 * Set this boolean to true to enable warning logging
	 */
	public static final boolean WARNING = true;

	/**
	 * Set this boolean to true to enable wtf logging
	 */
	public static final boolean WTF = true;

	/**
	 * Clears the database everytime it is initialized
	 */
	public static final boolean DROP_TABLES_EVERY_TIME = false;

	public static final boolean SUPPORTS_GINGERBREAD;

	public static final boolean SUPPORTS_HONEYCOMB;

	public static final boolean SUPPORTS_FROYO;

	public static final int SHARED_PREFS_MODE;

	/**
	 * The amount of intersecting that is needed between a users accuracy radius
	 * and a ringers location radius
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

	/**
	 * the update interval in ms
	 */
	public static final long UPDATE_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	/**
	 * minum accracy required to report in meters
	 */
	public static final int ACCURACY = 100;

	/**
	 * all lolcations with an accuracy greater then this will be ignored. in
	 * meters
	 */
	public static final int IGNORE = 500;

	static {
		SUPPORTS_FROYO = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;

		SUPPORTS_GINGERBREAD = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;

		SUPPORTS_HONEYCOMB = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;

		SHARED_PREFS_MODE = SUPPORTS_HONEYCOMB ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE;
	}
}