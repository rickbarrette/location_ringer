/**
 * GetLocationWidget.java
 * @date Jun 27, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.receivers;

import org.RickBarrette.android.LocationRinger.Constraints;
import org.RickBarrette.android.LocationRinger.Log;
import org.RickBarrette.android.LocationRinger.R;
import org.RickBarrette.android.LocationRinger.services.LocationService;
import org.RickBarrette.android.LocationRinger.ui.SettingsActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * This widget will be used to force a Location update from the users home
 * screen
 * 
 * @author ricky barrette
 */
public class GetLocationWidget extends AppWidgetProvider {

	public final String TAG = "GetLocationWidget";

	public static final String ACTION_UPDATE = "action_update";

	/**
	 * Called in response to the ACTION_APPWIDGET_DELETED broadcast when one or
	 * more AppWidget instances have been deleted. Override this method to
	 * implement your own AppWidget functionality. (non-Javadoc)
	 * 
	 * @see android.appwidget.AppWidgetProvider#onDeleted(android.content.Context,
	 *      int[])
	 * @param context
	 * @param appWidgetIds
	 * @author ricky barrette
	 */
	@Override
	public void onDeleted(final Context context, final int[] appWidgetIds) {
		Log.v(TAG, "onDelete()");
		super.onDeleted(context, appWidgetIds);
	}

	/**
	 * Implements onReceive(Context, Intent) to dispatch calls to the various
	 * other methods on AppWidgetProvider. (non-Javadoc)
	 * 
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context,
	 *      android.content.Intent)
	 * @param context
	 * @param intent
	 *            received
	 * @author ricky barrette
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.v(TAG, "onReceive");
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();

		if (action.equals(ACTION_UPDATE)) {
			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			onUpdate(context, mgr, mgr.getAppWidgetIds(new ComponentName(context, GetLocationWidget.class)));
		}

		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
				onDeleted(context, new int[] { appWidgetId });
		}
		super.onReceive(context, intent);
	}

	/**
	 * Called in response to the ACTION_APPWIDGET_UPDATE broadcast when this
	 * AppWidget provider is being asked to provide RemoteViews for a set of
	 * AppWidgets. Override this method to implement your own AppWidget
	 * functionality.
	 * 
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context,
	 *      android.appwidget.AppWidgetManager, int[])
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetIds
	 * @author ricky barrette
	 */
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		Log.v(TAG, "onUpdate()");
		final int N = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			final int appWidgetId = appWidgetIds[i];

			final Intent intent = LocationService.getSingleShotServiceIntent(context);

			// create a pending intent to start the post activity
			final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.get_location_widget);
			views.setOnClickPendingIntent(R.id.widget_get_location_button, pendingIntent);

			views.setTextViewText(
					R.id.widget_label,
					context.getSharedPreferences(SettingsActivity.SETTINGS, Constraints.SHARED_PREFS_MODE).getString(SettingsActivity.CURRENT,
							context.getString(R.string.default_ringer)));

			// Tell the AppWidgetManager to perform an update on the current App
			// Widget
			appWidgetManager.updateAppWidget(appWidgetId, views);

		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
