/**
 * @author Twenty Codes
 * @author ricky barrette
 */
package org.RickBarrette.android.LocationRinger.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * this class will be a simple TextView to be used in a preference activity. you
 * set the text using the set title tag
 * 
 * @author ricky barrette
 */
public class TextViewPreference extends Preference {

	/**
	 * creates a preference that is nothing but a text view
	 * 
	 * @param context
	 */
	public TextViewPreference(final Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * creates a preference that is nothing but a text view
	 * 
	 * @param context
	 * @param attrs
	 */
	public TextViewPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * creates a preference that is nothing but a text view
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TextViewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * creates a linear layout the contains only a textview. (non-Javadoc)
	 * 
	 * @see android.preference.Preference#onCreateView(android.view.ViewGroup)
	 * @param parent
	 * @return
	 * @author ricky barrette
	 */
	@Override
	protected View onCreateView(final ViewGroup parent) {

		/*
		 * create a vertical linear layout that width and height that wraps
		 * content
		 */
		final LinearLayout layout = new LinearLayout(getContext());
		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.removeAllViews();

		/*
		 * create a textview that will be used to display the title provided in
		 * xml and add it to the lay out
		 */
		final TextView title = new TextView(getContext());
		title.setText(getTitle());
		title.setTextSize(16);
		title.setTypeface(Typeface.SANS_SERIF);
		title.setGravity(Gravity.LEFT);
		title.setLayoutParams(params);

		/*
		 * add the title and the time picker views to the layout
		 */
		layout.addView(title);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}
}
