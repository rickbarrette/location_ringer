/**
 * FeatureFragment.java
 * @date May 26, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package org.RickBarrette.android.LocationRinger.ui.fragments;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import org.RickBarrette.android.LocationRinger.FeatureRemovedListener;
import org.RickBarrette.android.LocationRinger.R;

/**
 * This is a simple extention of a fragment that will allow for storage of an id
 * 
 * @author ricky barrette
 */
@SuppressLint("ValidFragment")
public class BaseFeatureFragment extends Fragment implements OnClickListener {

	private final int mId;
	private final FeatureRemovedListener mRemovedListener;
	private final int mIconRes;
	private final int mLayout;
	private ImageView mIcon;

	/**
	 * Creates a new Feature Fragment
	 * 
	 * @param id
	 * @param layout
	 * @param listener
	 * @author ricky barrette
	 */
	public BaseFeatureFragment(final int id, final int layout, final FeatureRemovedListener listener) {
		this(id, layout, -1, listener);
	}

	/**
	 * Creates a new FeatureFragment
	 * 
	 * @param id
	 * @param layout
	 * @param icon
	 * @param listener
	 * @author ricky barrette
	 */
	public BaseFeatureFragment(final int id, final int layout, final int icon, final FeatureRemovedListener listener) {
		super();
		if (listener == null)
			throw new NullPointerException();
		mRemovedListener = listener;
		mId = id;
		mIconRes = icon;
		mLayout = layout;
	}

	/**
	 * @return the id of this fragment
	 */
	public int getFragmentId() {
		return mId;
	}

	/**
	 * Called when the user clicks the remove button
	 * 
	 * @param v
	 * @author ricky barrette
	 */
	@Override
	public void onClick(final View v) {
		if (v.getId() == R.id.close)
			if (mRemovedListener != null)
				mRemovedListener.onFeatureRemoved(this);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(mLayout, container, false);
		mIcon = (ImageView) view.findViewById(R.id.icon);
		view.findViewById(R.id.close).setOnClickListener(this);
		if (mIconRes != -1)
			setIcon(mIconRes);
		return view;
	}

	/**
	 * Sets the icon of this feature fragment
	 * 
	 * @param icon
	 * @author ricky barrette
	 */
	public void setIcon(final int icon) {
		mIcon.setImageDrawable(getActivity().getResources().getDrawable(icon));
	}
}