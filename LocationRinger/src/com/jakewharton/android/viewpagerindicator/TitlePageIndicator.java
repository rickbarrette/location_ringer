/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Francisco Figueiredo Jr.
 * Copyright (C) 2011 Jake Wharton
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
 * limitations under the License.
 */
package com.jakewharton.android.viewpagerindicator;

import java.util.ArrayList;

import org.RickBarrette.android.LocationRinger.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * A TitlePageIndicator is a PageIndicator which displays the title of left view
 * (if exist), the title of the current select view (centered) and the title of
 * the right view (if exist). When the user scrolls the ViewPager then titles
 * are also scrolled.
 */
public class TitlePageIndicator extends TextView implements PageIndicator, View.OnTouchListener {
	public enum IndicatorStyle {
		None(0), Triangle(1), Underline(2);

		public static IndicatorStyle fromValue(final int value) {
			for (final IndicatorStyle style : IndicatorStyle.values())
				if (style.value == value)
					return style;
			return null;
		}

		public final int value;

		private IndicatorStyle(final int value) {
			this.value = value;
		}
	}

	static class SavedState extends BaseSavedState {
		int currentPage;

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		private SavedState(final Parcel in) {
			super(in);
			currentPage = in.readInt();
		}

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPage);
		}
	}

	private static final float UNDERLINE_FADE_PERCENTAGE = 0.25f;
	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mListener;
	private TitleProvider mTitleProvider;
	private int mCurrentPage;
	private int mCurrentOffset;
	private final Paint mPaintText;
	private final Paint mPaintSelected;
	private final Path mPath;
	private final Paint mPaintFooterLine;
	private IndicatorStyle mFooterIndicatorStyle;
	private final Paint mPaintFooterIndicator;
	private float mFooterIndicatorHeight;
	private final float mFooterIndicatorPadding;
	private final float mFooterIndicatorUnderlinePadding;
	private float mTitlePadding;
	/** Left and right side padding for not active view titles. */
	private float mClipPadding;

	private float mFooterLineHeight;

	public TitlePageIndicator(final Context context) {
		this(context, null);
	}

	public TitlePageIndicator(final Context context, final AttributeSet attrs) {
		this(context, attrs, R.attr.titlePageIndicatorStyle);
	}

	public TitlePageIndicator(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		super.setOnTouchListener(this);
		mPath = new Path();

		// Load defaults from resources
		final Resources res = getResources();
		final int defaultFooterColor = res.getColor(R.color.default_title_indicator_footer_color);
		final float defaultFooterLineHeight = res.getDimension(R.dimen.default_title_indicator_footer_line_height);
		final int defaultFooterIndicatorStyle = res.getInteger(R.integer.default_title_indicator_footer_indicator_style);
		final float defaultFooterIndicatorHeight = res.getDimension(R.dimen.default_title_indicator_footer_indicator_height);
		final float defaultFooterIndicatorPadding = res.getDimension(R.dimen.default_title_indicator_footer_indicator_padding);
		final float defaultFooterIndicatorUnderlinePadding = res.getDimension(R.dimen.default_title_indicator_footer_indicator_underline_padding);
		final int defaultSelectedColor = res.getColor(R.color.default_title_indicator_selected_color);
		final boolean defaultSelectedBold = res.getBoolean(R.bool.default_title_indicator_selected_bold);
		final int defaultTextColor = res.getColor(R.color.default_title_indicator_text_color);
		final float defaultTextSize = res.getDimension(R.dimen.default_title_indicator_text_size);
		final float defaultTitlePadding = res.getDimension(R.dimen.default_title_indicator_title_padding);
		final float defaultClipPadding = res.getDimension(R.dimen.default_title_indicator_clip_padding);

		// Retrieve styles attributes
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitlePageIndicator, defStyle, R.style.Widget_TitlePageIndicator);

		// Retrieve the colors to be used for this view and apply them.
		mFooterLineHeight = a.getDimension(R.styleable.TitlePageIndicator_footerLineHeight, defaultFooterLineHeight);
		mFooterIndicatorStyle = IndicatorStyle.fromValue(a.getInteger(R.styleable.TitlePageIndicator_footerIndicatorStyle, defaultFooterIndicatorStyle));
		mFooterIndicatorHeight = a.getDimension(R.styleable.TitlePageIndicator_footerIndicatorHeight, defaultFooterIndicatorHeight);
		mFooterIndicatorPadding = a.getDimension(R.styleable.TitlePageIndicator_footerIndicatorPadding, defaultFooterIndicatorPadding);
		mFooterIndicatorUnderlinePadding = a.getDimension(R.styleable.TitlePageIndicator_footerIndicatorUnderlinePadding, defaultFooterIndicatorUnderlinePadding);
		mTitlePadding = a.getDimension(R.styleable.TitlePageIndicator_titlePadding, defaultTitlePadding);
		mClipPadding = a.getDimension(R.styleable.TitlePageIndicator_clipPadding, defaultClipPadding);

		final float textSize = a.getDimension(R.styleable.TitlePageIndicator_textSize, defaultTextSize);
		final int footerColor = a.getColor(R.styleable.TitlePageIndicator_footerColor, defaultFooterColor);
		mPaintText = new Paint();
		mPaintText.setColor(a.getColor(R.styleable.TitlePageIndicator_textColor, defaultTextColor));
		mPaintText.setTextSize(textSize);
		mPaintText.setAntiAlias(true);
		mPaintSelected = new Paint();
		mPaintSelected.setColor(a.getColor(R.styleable.TitlePageIndicator_selectedColor, defaultSelectedColor));
		mPaintSelected.setTextSize(textSize);
		mPaintSelected.setFakeBoldText(a.getBoolean(R.styleable.TitlePageIndicator_selectedBold, defaultSelectedBold));
		mPaintSelected.setAntiAlias(true);
		mPaintFooterLine = new Paint();
		mPaintFooterLine.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaintFooterLine.setStrokeWidth(mFooterLineHeight);
		mPaintFooterLine.setColor(footerColor);
		mPaintFooterIndicator = new Paint();
		mPaintFooterIndicator.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaintFooterIndicator.setColor(footerColor);

		a.recycle();
	}

	/**
	 * Calculate the bounds for a view's title
	 * 
	 * @param index
	 * @param paint
	 * @return
	 */
	private Rect calcBounds(final int index, final Paint paint) {
		// Calculate the text bounds
		final Rect bounds = new Rect();
		bounds.right = (int) paint.measureText(mTitleProvider.getTitle(index));
		bounds.bottom = (int) (paint.descent() - paint.ascent());
		return bounds;
	}

	/**
	 * Calculate views bounds and scroll them according to the current index
	 * 
	 * @param paint
	 * @param currentIndex
	 * @return
	 */
	private ArrayList<Rect> calculateAllBounds(final Paint paint) {
		final ArrayList<Rect> list = new ArrayList<Rect>();
		// For each views (If no values then add a fake one)
		final int count = mViewPager.getAdapter().getCount();
		final int width = getWidth();
		final int halfWidth = width / 2;
		for (int i = 0; i < count; i++) {
			final Rect bounds = calcBounds(i, paint);
			final int w = bounds.right - bounds.left;
			final int h = bounds.bottom - bounds.top;
			bounds.left = halfWidth - w / 2 - mCurrentOffset + (i - mCurrentPage) * width;
			bounds.right = bounds.left + w;
			bounds.top = 0;
			bounds.bottom = h;
			list.add(bounds);
		}

		return list;
	}

	/**
	 * Set bounds for the left textView including clip padding.
	 * 
	 * @param curViewBound
	 *            current bounds.
	 * @param curViewWidth
	 *            width of the view.
	 */
	private void clipViewOnTheLeft(final Rect curViewBound, final int curViewWidth) {
		curViewBound.left = 0 + (int) mClipPadding;
		curViewBound.right = curViewWidth;
	}

	/**
	 * Set bounds for the right textView including clip padding.
	 * 
	 * @param curViewBound
	 *            current bounds.
	 * @param curViewWidth
	 *            width of the view.
	 */
	private void clipViewOnTheRight(final Rect curViewBound, final int curViewWidth, final int leftPlusWidth) {
		curViewBound.right = leftPlusWidth - (int) mClipPadding;
		curViewBound.left = curViewBound.right - curViewWidth;
	}

	public float getClipPadding() {
		return mClipPadding;
	}

	public int getFooterColor() {
		return mPaintFooterLine.getColor();
	}

	public float getFooterIndicatorHeight() {
		return mFooterIndicatorHeight;
	}

	public IndicatorStyle getFooterIndicatorStyle() {
		return mFooterIndicatorStyle;
	}

	public float getFooterLineHeight() {
		return mFooterLineHeight;
	}

	public int getSelectedColor() {
		return mPaintSelected.getColor();
	}

	public int getTextColor() {
		return mPaintText.getColor();
	}

	@Override
	public float getTextSize() {
		return mPaintText.getTextSize();
	}

	public float getTitlePadding() {
		return mTitlePadding;
	}

	public boolean isSelectedBold() {
		return mPaintSelected.isFakeBoldText();
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(final int measureSpec) {
		float result = 0;
		final int specMode = MeasureSpec.getMode(measureSpec);
		final int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY)
			// We were told how big to be
			result = specSize;
		else {
			// Calculate the text bounds
			final Rect bounds = new Rect();
			bounds.bottom = (int) (mPaintText.descent() - mPaintText.ascent());
			result = bounds.bottom - bounds.top + mFooterLineHeight;
			if (mFooterIndicatorStyle != IndicatorStyle.None)
				result += mFooterIndicatorHeight + mFooterIndicatorPadding;
		}
		return (int) result;
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(final int measureSpec) {
		int result = 0;
		final int specMode = MeasureSpec.getMode(measureSpec);
		final int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode != MeasureSpec.EXACTLY)
			throw new IllegalStateException(getClass().getSimpleName() + " can only be used in EXACTLY mode.");
		result = specSize;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		// Calculate views bounds
		final ArrayList<Rect> bounds = calculateAllBounds(mPaintText);

		final int count = mViewPager.getAdapter().getCount();
		final int countMinusOne = count - 1;
		final int halfWidth = getWidth() / 2;
		final int left = getLeft();
		final int width = getWidth();
		final int height = getHeight();
		final int leftPlusWidth = left + width;

		// Verify if the current view must be clipped to the screen
		final Rect curViewBound = bounds.get(mCurrentPage);
		final int curViewWidth = curViewBound.right - curViewBound.left;
		if (curViewBound.left < 0)
			// Try to clip to the screen (left side)
			clipViewOnTheLeft(curViewBound, curViewWidth);
		if (curViewBound.right > leftPlusWidth)
			// Try to clip to the screen (right side)
			clipViewOnTheRight(curViewBound, curViewWidth, leftPlusWidth);

		// Left views starting from the current position
		if (mCurrentPage > 0)
			for (int i = mCurrentPage - 1; i >= 0; i--) {
				final Rect bound = bounds.get(i);
				final int w = bound.right - bound.left;
				// Is left side is outside the screen
				if (bound.left < 0) {
					// Try to clip to the screen (left side)
					clipViewOnTheLeft(bound, w);
					// Except if there's an intersection with the right view
					if (i < countMinusOne && mCurrentPage != i) {
						final Rect rightBound = bounds.get(i + 1);
						// Intersection
						if (bound.right + (int) mTitlePadding > rightBound.left)
							bound.left = rightBound.left - (w + (int) mTitlePadding);
					}
				}
			}
		// Right views starting from the current position
		if (mCurrentPage < countMinusOne)
			for (int i = mCurrentPage + 1; i < count; i++) {
				final Rect bound = bounds.get(i);
				final int w = bound.right - bound.left;
				// If right side is outside the screen
				if (bound.right > leftPlusWidth) {
					// Try to clip to the screen (right side)
					clipViewOnTheRight(bound, w, leftPlusWidth);
					// Except if there's an intersection with the left view
					if (i > 0 && mCurrentPage != i) {
						final Rect leftBound = bounds.get(i - 1);
						// Intersection
						if (bound.left - (int) mTitlePadding < leftBound.right)
							bound.left = leftBound.right + (int) mTitlePadding;
					}
				}
			}

		// Now draw views
		for (int i = 0; i < count; i++) {
			// Get the title
			final Rect bound = bounds.get(i);
			// Only if one side is visible
			if (bound.left > left && bound.left < leftPlusWidth || bound.right > left && bound.right < leftPlusWidth) {
				Paint paint = mPaintText;
				// Change the color is the title is closed to the center
				final int middle = (bound.left + bound.right) / 2;
				if (Math.abs(middle - halfWidth) < 20)
					paint = mPaintSelected;
				canvas.drawText(mTitleProvider.getTitle(i), bound.left, bound.bottom, paint);
			}
		}

		// Draw the footer line
		mPath.reset();
		mPath.moveTo(0, height - mFooterLineHeight);
		mPath.lineTo(width, height - mFooterLineHeight);
		mPath.close();
		canvas.drawPath(mPath, mPaintFooterLine);

		switch (mFooterIndicatorStyle) {
		default:
		case Triangle:
			mPath.reset();
			mPath.moveTo(halfWidth, height - mFooterLineHeight - mFooterIndicatorHeight);
			mPath.lineTo(halfWidth + mFooterIndicatorHeight, height - mFooterLineHeight);
			mPath.lineTo(halfWidth - mFooterIndicatorHeight, height - mFooterLineHeight);
			mPath.close();
			canvas.drawPath(mPath, mPaintFooterIndicator);
			break;

		case Underline:
			final float deltaPercentage = mCurrentOffset * 1.0f / width;
			int alpha = 0xFF;
			int page = mCurrentPage;
			if (deltaPercentage <= UNDERLINE_FADE_PERCENTAGE)
				alpha = (int) (0xFF * ((UNDERLINE_FADE_PERCENTAGE - deltaPercentage) / UNDERLINE_FADE_PERCENTAGE));
			else if (deltaPercentage >= 1 - UNDERLINE_FADE_PERCENTAGE) {
				alpha = (int) (0xFF * ((deltaPercentage - (1 - UNDERLINE_FADE_PERCENTAGE)) / UNDERLINE_FADE_PERCENTAGE));
				page += 1; // We are coming into the next page
			} else if (mCurrentOffset != 0)
				break; // Not in underline scope

			final Rect underlineBounds = bounds.get(page);
			mPath.reset();
			mPath.moveTo(underlineBounds.left - mFooterIndicatorUnderlinePadding, height - mFooterLineHeight);
			mPath.lineTo(underlineBounds.right + mFooterIndicatorUnderlinePadding, height - mFooterLineHeight);
			mPath.lineTo(underlineBounds.right + mFooterIndicatorUnderlinePadding, height - mFooterLineHeight - mFooterIndicatorHeight);
			mPath.lineTo(underlineBounds.left - mFooterIndicatorUnderlinePadding, height - mFooterLineHeight - mFooterIndicatorHeight);
			mPath.close();

			mPaintFooterIndicator.setAlpha(alpha);
			canvas.drawPath(mPath, mPaintFooterIndicator);
			mPaintFooterIndicator.setAlpha(0xFF);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
		mCurrentPage = position;
		mCurrentOffset = positionOffsetPixels;
		invalidate();

		if (mListener != null)
			mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
	}

	@Override
	public void onPageScrollStateChanged(final int state) {
		if (mListener != null)
			mListener.onPageScrollStateChanged(state);
	}

	@Override
	public void onPageSelected(final int position) {
		if (mListener != null)
			mListener.onPageSelected(position);
	}

	@Override
	public void onRestoreInstanceState(final Parcelable state) {
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mCurrentPage = savedState.currentPage;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		setFreezesText(true);
		final Parcelable superState = super.onSaveInstanceState();
		final SavedState savedState = new SavedState(superState);
		savedState.currentPage = mCurrentPage;
		return savedState;
	}

	@Override
	public final boolean onTouch(final View view, final MotionEvent event) {
		if (view != this || event.getAction() != MotionEvent.ACTION_DOWN)
			return false;

		final int count = mViewPager.getAdapter().getCount();
		final float halfWidth = getWidth() / 2;
		final float sixthWidth = getWidth() / 6;

		if (mCurrentPage > 0 && event.getX() < halfWidth - sixthWidth) {
			mViewPager.setCurrentItem(mCurrentPage - 1);
			return true;
		} else if (mCurrentPage < count - 1 && event.getX() > halfWidth + sixthWidth) {
			mViewPager.setCurrentItem(mCurrentPage + 1);
			return true;
		}

		return false;
	}

	public void setClipPadding(final float clipPadding) {
		mClipPadding = clipPadding;
		invalidate();
	}

	@Override
	public void setCurrentItem(final int item) {
		if (mViewPager == null)
			throw new IllegalStateException("ViewPager has not been bound.");
		mViewPager.setCurrentItem(item);
		mCurrentPage = item;
		invalidate();
	}

	public void setFooterColor(final int footerColor) {
		mPaintFooterLine.setColor(footerColor);
		invalidate();
	}

	public void setFooterIndicatorHeight(final float footerTriangleHeight) {
		mFooterIndicatorHeight = footerTriangleHeight;
		invalidate();
	}

	public void setFooterIndicatorStyle(final IndicatorStyle indicatorStyle) {
		mFooterIndicatorStyle = indicatorStyle;
		invalidate();
	}

	public void setFooterLineHeight(final float footerLineHeight) {
		mFooterLineHeight = footerLineHeight;
		invalidate();
	}

	@Override
	public void setOnPageChangeListener(final ViewPager.OnPageChangeListener listener) {
		mListener = listener;
	}

	@Override
	public final void setOnTouchListener(final OnTouchListener listener) {
		throw new UnsupportedOperationException("This view does not support listening to its touch events.");
	}

	public void setSelectedBold(final boolean selectedBold) {
		mPaintSelected.setFakeBoldText(selectedBold);
		invalidate();
	}

	public void setSelectedColor(final int selectedColor) {
		mPaintSelected.setColor(selectedColor);
		invalidate();
	}

	@Override
	public void setTextColor(final int textColor) {
		mPaintText.setColor(textColor);
		invalidate();
	}

	@Override
	public void setTextSize(final float textSize) {
		mPaintText.setTextSize(textSize);
		invalidate();
	}

	public void setTitlePadding(final float titlePadding) {
		mTitlePadding = titlePadding;
		invalidate();
	}

	@Override
	public void setViewPager(final ViewPager view) {
		if (view.getAdapter() == null)
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		if (!(view.getAdapter() instanceof TitleProvider))
			throw new IllegalStateException("ViewPager adapter must implement TitleProvider to be used with TitlePageIndicator.");
		mViewPager = view;
		mViewPager.setOnPageChangeListener(this);
		mTitleProvider = (TitleProvider) mViewPager.getAdapter();
		invalidate();
	}

	@Override
	public void setViewPager(final ViewPager view, final int initialPosition) {
		setViewPager(view);
		setCurrentItem(initialPosition);
	}
}
