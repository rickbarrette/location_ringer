/**
 * @author Twenty Codes
 * @author ricky barrette
 * @date June 29, 2011
 */
package com.TwentyCodes.android.LocationRinger.ui;

import com.TwentyCodes.android.LocationRinger.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * this class will be a simple preference that contains only a text view that will display the application build information 
 * @author ricky barrette
 */
public class VersionInformationPreference extends Preference {

	private Context mContext;


	/**
	 * creates a preference that is nothing but a text view 
	 * @param context
	 */
	public VersionInformationPreference(Context context) {
		super(context);
		mContext = context;
	}
	
	/**
	 * creates a preference that is nothing but a text view
	 * @param context
	 * @param attrs
	 */
	public VersionInformationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	/**
	 * creates a preference that is nothing but a text view
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public VersionInformationPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	
	/**
	 * creates a linear layout the contains only a textview.
	 * (non-Javadoc)
	 * @see android.preference.Preference#onCreateView(android.view.ViewGroup)
	 * @param parent
	 * @return
	 * @author ricky barrette
	 */
	@Override
	 protected View onCreateView(ViewGroup parent){
		
		/*
		 * get the build information, and build the string
		 */
		PackageManager pm = mContext.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException eNnf) {
			//doubt this will ever run since we want info about our own package
			pi = new PackageInfo();
			pi.versionName = "unknown";
			pi.versionCode = 1;
		}
		
		/*
		 * create a vertical linear layout that width and height that wraps content
		 */
		LinearLayout layout = new LinearLayout(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//		params.gravity = Gravity.CENTER;
		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.removeAllViews();
		
		/*
		 * create a textview that will be used to display the application's name and build information
		 * and add it to the layout
		 */
		TextView title = new TextView(getContext());
		title.setText(mContext.getString(R.string.app_name)+" "+pi.versionName+" bulid "+pi.versionCode);
		title.setTextSize(16);
		title.setTypeface(Typeface.SANS_SERIF);
		title.setGravity(Gravity.LEFT);
		title.setLayoutParams(params);

		/*
		 * add the title views to the layout
		 */
		layout.addView(title);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}
}
