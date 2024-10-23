package com.smart.album.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import androidx.appcompat.widget.AppCompatImageView;

public class SlideImageView extends AppCompatImageView {

    private OnPanningEndListener panningEndListener;

    public SlideImageView(Context context) {
        super(context);
    }

    public SlideImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startPanningIfNecessary();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        startPanningIfNecessary();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        startPanningIfNecessary();
        return result;
    }

    private void startPanningIfNecessary() {
        if (getDrawable() != null && getWidth() > 0 && getHeight() > 0) {
            Drawable drawable = getDrawable();
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();

            Log.d("pan===","width=="+getWidth());
            Log.d("pan===","height=="+getHeight());
            Log.d("pan===","drawableWidth=="+drawableWidth);
            Log.d("pan===","drawableHeight=="+drawableHeight);
            if (drawableWidth > getWidth() || drawableHeight > getHeight()) {
                // 图片尺寸超过了屏幕大小
                panImage(drawableWidth, drawableHeight);
            }
        }
    }

    private void panImage(int drawableWidth, int drawableHeight) {
        float fromXDelta = 0.0f;
        float toXDelta = 0.0f;
        float fromYDelta = 0.0f;
        float toYDelta = 0.0f;

        if (drawableWidth > getWidth()) {
            toXDelta = -getWidth();
        }

        if (drawableHeight > getHeight()) {
            toYDelta = -(drawableHeight -getHeight());
        }

        TranslateAnimation animation = new TranslateAnimation(
                fromXDelta, toXDelta,
                fromYDelta, toYDelta
        );


        Log.d("pan===","fromXDelta=="+fromXDelta);
        Log.d("pan===","toXDelta=="+toXDelta);
        Log.d("pan===","fromYDelta=="+fromYDelta);
        Log.d("pan===","toYDelta=="+toYDelta);
        animation.setDuration(15000); // 动画持续时间
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (panningEndListener != null) {
                    panningEndListener.onPanningEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        startAnimation(animation);
    }

    public void setOnPanningEndListener(OnPanningEndListener listener) {
        this.panningEndListener = listener;
    }

    public interface OnPanningEndListener {
        void onPanningEnd();
    }
}
