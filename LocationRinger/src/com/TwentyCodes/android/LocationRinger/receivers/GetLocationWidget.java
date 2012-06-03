/**
 * GetLocationWidget.java
 * @date Jun 27, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;
import com.TwentyCodes.android.LocationRinger.services.LocationService;
import com.TwentyCodes.android.LocationRinger.ui.SettingsActivity;
import com.TwentyCodes.android.debug.LocationLibraryConstants;

/**
 * This widget will be used to force a Location update from the users home screen
 * @author ricky barrette
 */
public class GetLocationWidget extends AppWidgetProvider {

public final String TAG = "GetLocationWidget";
	
	/**
	 * Called in response to the ACTION_APPWIDGET_UPDATE broadcast when this AppWidget provider is being asked to provide RemoteViews for a set of AppWidgets. 
	 * Override this method to implement your own AppWidget functionality. 
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetIds
	 * @author ricky barrette
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		if (Debug.DEBUG)
			Log.v(TAG, "onUpdate()");
		final int N = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            
            Intent intent = new Intent(context, LocationService.class)
			.putExtra(LocationService.INTENT_EXTRA_REQUIRED_ACCURACY, Integer.parseInt(context.getSharedPreferences(SettingsActivity.SETTINGS, Debug.SHARED_PREFS_MODE).getString(SettingsActivity.ACCURACY , "50")))
            .setAction(LocationLibraryConstants.INTENT_ACTION_UPDATE);
                        
            //create a pending intent to start the post activity
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
            
            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.get_location_widget);
            views.setOnClickPendingIntent(R.id.widget_get_location_button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            
        }
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	/**
	 * Implements onReceive(Context, Intent) to dispatch calls to the various other methods on AppWidgetProvider.
	 * (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 * @param context
	 * @param intent received
	 * @author ricky barrette
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Debug.DEBUG)
			Log.v(TAG, "onReceive");
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}
	}
	
	/**
	 * Called in response to the ACTION_APPWIDGET_DELETED broadcast when one or more AppWidget instances have been deleted. 
	 * Override this method to implement your own AppWidget functionality. 
	 * (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onDeleted(android.content.Context, int[])
	 * @param context
	 * @param appWidgetIds
	 * @author ricky barrette
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		if (Debug.DEBUG)
			Log.v(TAG, "onDelete()");
		super.onDeleted(context, appWidgetIds);
	}
	
}
