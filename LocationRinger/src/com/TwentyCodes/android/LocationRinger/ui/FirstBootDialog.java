/**
 * FirstBootDialog.java
 * @date Jul 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.TwentyCodes.android.LocationRinger.R;
import com.TwentyCodes.android.LocationRinger.debug.Debug;

/**
 * This class will be used to display the first boot dialog
 * 
 * @author ricky barrette
 */
public class FirstBootDialog extends Dialog implements android.view.View.OnClickListener {

	/**
	 * Creates a new FirstBootDialog
	 * 
	 * @param context
	 * @author ricky barrette
	 */
	public FirstBootDialog(final Context context) {
		super(context);
		build(context);
	}

	/**
	 * Creates a new FirstBootDialog
	 * 
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 * @author ricky barrette
	 */
	public FirstBootDialog(final Context context, final boolean cancelable, final OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		build(context);
	}

	/**
	 * Creates a new FirstBootDialog
	 * 
	 * @param context
	 * @param theme
	 * @author ricky barrette
	 */
	public FirstBootDialog(final Context context, final int theme) {
		super(context, theme);
		build(context);
	}

	/**
	 * Builds the dialog
	 * 
	 * @param context
	 * @author ricky barrette
	 */
	private void build(final Context context) {
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		this.setContentView(R.layout.first_boot_dialog);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
		this.setTitle(R.string.welcome);
		findViewById(R.id.ok_button).setOnClickListener(this);
	}

	/**
	 * called when the ok button is clicked
	 */
	@Override
	public void onClick(final View arg0) {
		getContext().getSharedPreferences(SettingsActivity.SETTINGS, Debug.SHARED_PREFS_MODE).edit().putBoolean(SettingsActivity.IS_FIRST_BOOT, false).commit();
		dismiss();
	}

}