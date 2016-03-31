package com.creations.meister.jungleexplorer.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.creations.meister.jungleexplorer.R;

public class CircularAnimalView extends ViewSwitcher {
    //  private static final int DEFAULT_CONTENT_SIZE_IN_DP=20;
    private ImageView mImageView;
    private TextView mTextView;
    private Bitmap mBitmap;
    private CharSequence mText;
    private int mBackgroundColor = 0, mImageResId = 0;
    private int mContentSize;

    public CircularAnimalView(final Context context) {
        this(context, null);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public CircularAnimalView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        this.addView(this.mImageView = new ImageView(context),
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, Gravity.CENTER));
        this.addView(this.mTextView = new TextView(context),
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, Gravity.CENTER));

        this.mTextView.setGravity(Gravity.CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            this.mTextView.setAllCaps(true);

        this.mContentSize = getResources().getDimensionPixelSize(
                R.dimen.list_item__contact_imageview_size);

        if (isInEditMode())
            this.setTextAndBackgroundColor("", 0xFFff0000);
    }

    public void setContentSize(final int contentSize) {
        this.mContentSize = contentSize;
    }

    @SuppressWarnings("deprecation")
    private void drawContent(final int viewWidth, final int viewHeight) {
        ShapeDrawable roundedBackgroundDrawable = null;
        if (this.mBackgroundColor != 0) {
            roundedBackgroundDrawable = new ShapeDrawable(new OvalShape());
            roundedBackgroundDrawable.getPaint().setColor(this.mBackgroundColor);
            roundedBackgroundDrawable.setIntrinsicHeight(viewHeight);
            roundedBackgroundDrawable.setIntrinsicWidth(viewWidth);
            roundedBackgroundDrawable.setBounds(new Rect(0, 0, viewWidth, viewHeight));
        }
        if (this.mImageResId != 0) {
            this.mImageView.setBackgroundDrawable(roundedBackgroundDrawable);
            this.mImageView.setImageResource(this.mImageResId);
            this.mImageView.setScaleType(ScaleType.CENTER_INSIDE);
        } else if (mText != null) {
            this.mTextView.setText(mText);
            this.mTextView.setBackgroundDrawable(roundedBackgroundDrawable);
            this.mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, viewHeight / 2);
        } else if (this.mBitmap != null) {
            this.mImageView.setScaleType(ScaleType.FIT_CENTER);
            this.mImageView.setBackgroundDrawable(roundedBackgroundDrawable);
            if (this.mBitmap.getWidth() != this.mBitmap.getHeight())
                this.mBitmap = ThumbnailUtils.extractThumbnail(this.mBitmap, viewWidth, viewHeight);
            final RoundedBitmapDrawable roundedBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(getResources(), this.mBitmap);
            roundedBitmapDrawable.setCornerRadius(
                    (this.mBitmap.getWidth() + this.mBitmap.getHeight()) / 4);
            this.mImageView.setImageDrawable(roundedBitmapDrawable);
        }
        this.resetValuesState(false);
    }

    public void setTextAndBackgroundColor(final CharSequence text, final int backgroundColor) {
        this.resetValuesState(true);
        while (getCurrentView() != this.mTextView)
            showNext();
        this.mBackgroundColor = backgroundColor;
        this.mText = text;
        this.drawContent(this.mContentSize, this.mContentSize);
    }

    public void setImageResource(final int imageResId, final int backgroundColor) {
        this.resetValuesState(true);
        while (getCurrentView() != mImageView)
            showNext();
        this.mImageResId = imageResId;
        this.mBackgroundColor = backgroundColor;
        this.drawContent(this.mContentSize, this.mContentSize);
    }

    public void setImageBitmap(final Bitmap bitmap) {
        this.setImageBitmapAndBackgroundColor(bitmap, 0);
    }

    public void setImageBitmapAndBackgroundColor(final Bitmap bitmap, final int backgroundColor) {
        this.resetValuesState(true);
        while (this.getCurrentView() != this.mImageView)
            showNext();
        this.mBackgroundColor = backgroundColor;
        this.mBitmap = bitmap;
        this.drawContent(mContentSize, mContentSize);
    }

    private void resetValuesState(final boolean alsoResetViews) {
        this.mBackgroundColor = this.mImageResId = 0;
        this.mBitmap = null;
        this.mText = null;
        if (alsoResetViews) {
            this.mTextView.setText(null);
            this.mTextView.setBackgroundDrawable(null);
            this.mImageView.setImageBitmap(null);
            this.mImageView.setBackgroundDrawable(null);
        }
    }

    public ImageView getImageView() {
        return this.mImageView;
    }

    public TextView getTextView() {
        return this.mTextView;
    }

}