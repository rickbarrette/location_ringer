/**
 * FirstBootDialog.java
 * @date Jul 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.ui;

import com.TwentyCodes.android.LocationRinger.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

/**
 * This class will be used to display the first boot dialog
 * @author ricky barrette
 */
public class FirstBootDialog extends Dialog implements android.view.View.OnClickListener {

	/**
	 * Creates a new FirstBootDialog
	 * @param context
	 * @author ricky barrette
	 */
	public FirstBootDialog(Context context) {
		super(context);
		build(context);
	}


	/**
	 * Creates a new FirstBootDialog
	 * @param context
	 * @param theme
	 * @author ricky barrette
	 */
	public FirstBootDialog(Context context, int theme) {
		super(context, theme);
		build(context);
	}

	/**
	 * Creates a new FirstBootDialog
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 * @author ricky barrette
	 */
	public FirstBootDialog(Context context, boolean cancelable,	OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		build(context);
	}

	/**
	 * Builds the dialog
	 * @param context
	 * @author ricky barrette
	 */
	private void build(Context context) {
		this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		this.setContentView(R.layout.first_boot_dialog);
		this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
		this.setTitle(R.string.welcome);
		this.findViewById(R.id.ok_button).setOnClickListener(this);
	}

	/**
	 * called when the ok button is clicked
	 */
	@Override
	public void onClick(View arg0) {
		this.getContext().getSharedPreferences(SettingsActivity.SETTINGS, Context.MODE_WORLD_WRITEABLE).edit().putBoolean(SettingsActivity.IS_FIRST_BOOT, false).commit();
		this.dismiss();
	}

}