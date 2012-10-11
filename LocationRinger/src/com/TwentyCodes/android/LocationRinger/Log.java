/**
 * Log.java
 * @date Sep 14, 2012
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
package com.TwentyCodes.android.LocationRinger;

/**
 * A convince class for logging with log level constraints
 * 
 * @author ricky barrette
 */
public class Log {

	/**
	 * Send a DEBUG log message
	 * 
	 * @param tag
	 * @param log
	 * @author ricky barrette
	 */
	public static void d(final String tag, final String log) {
		if (Constraints.DEBUG)
			android.util.Log.d(tag, log);
	}

	/**
	 * Send a DEBUG log message and log the exception.
	 * 
	 * @param tag
	 * @param log
	 * @param e
	 * @author ricky barrette
	 */
	public static void d(final String tag, final String log, final Throwable e) {
		if (Constraints.DEBUG)
			android.util.Log.d(tag, log, e);
	}

	/**
	 * Send a ERROR log message
	 * 
	 * @param tag
	 * @param log
	 * @author ricky barrette
	 */
	public static void e(final String tag, final String log) {
		if (Constraints.ERROR)
			android.util.Log.e(tag, log);
	}

	/**
	 * Send a ERROR log message and log the exception.
	 * 
	 * @param tag
	 * @param log
	 * @param e
	 * @author ricky barrette
	 */
	public static void e(final String tag, final String log, final Throwable e) {
		if (Constraints.ERROR)
			android.util.Log.e(tag, log, e);
	}

	/**
	 * Send a INFO log message
	 * 
	 * @param tag
	 * @param log
	 * @author ricky barrette
	 */
	public static void i(final String tag, final String log) {
		if (Constraints.INFO)
			android.util.Log.i(tag, log);
	}

	/**
	 * Send a INFO log message and log the exception.
	 * 
	 * @param tag
	 * @param log
	 * @param e
	 * @author ricky barrette
	 */
	public static void i(final String tag, final String log, final Throwable e) {
		if (Constraints.INFO)
			android.util.Log.i(tag, log, e);
	}

	/**
	 * Send a VERBOSE log message
	 * 
	 * @param tag
	 * @param log
	 * @author ricky barrette
	 */
	public static void v(final String tag, final String log) {
		if (Constraints.VERBOSE)
			android.util.Log.v(tag, log);
	}

	/**
	 * Send a VERBOSE log message and log the exception.
	 * 
	 * @param tag
	 * @param log
	 * @param e
	 * @author ricky barrette
	 */
	public static void v(final String tag, final String log, final Throwable e) {
		if (Constraints.VERBOSE)
			android.util.Log.v(tag, log, e);
	}

	/**
	 * Send a WARNING log message
	 * 
	 * @param tag
	 * @param log
	 * @author ricky barrette
	 */
	public static void w(final String tag, final String log) {
		if (Constraints.WARNING)
			android.util.Log.w(tag, log);
	}

	/**
	 * Send a WARNING log message and log the exception.
	 * 
	 * @param tag
	 * @param log
	 * @param e
	 * @author ricky barrette
	 */
	public static void w(final String tag, final String log, final Throwable e) {
		if (Constraints.WARNING)
			android.util.Log.w(tag, log, e);
	}

	/**
	 * Send a WTF log message
	 * 
	 * @param tag
	 * @param log
	 * @author ricky barrette
	 */
	public static void wtf(final String tag, final String log) {
		if (Constraints.WTF)
			android.util.Log.wtf(tag, log);
	}

	/**
	 * Send a WTF log message and log the exception.
	 * 
	 * @param tag
	 * @param log
	 * @param e
	 * @author ricky barrette
	 */
	public static void wtf(final String tag, final String log, final Throwable e) {
		if (Constraints.WTF)
			android.util.Log.wtf(tag, log, e);
	}
}