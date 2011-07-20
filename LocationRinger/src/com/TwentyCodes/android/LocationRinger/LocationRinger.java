/**
 * LocationRinger.java
 * @date Apr 29, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger;

import android.os.Bundle;

import com.TwentyCodes.android.LocationRinger.ui.RingerListActivity;
import com.TwentyCodes.android.exception.ExceptionHandler;

/**
 * This is the main Activity for Location Ringer
 * @author ricky barrette
 */
public class LocationRinger extends RingerListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		super.onCreate(savedInstanceState);
	}
}