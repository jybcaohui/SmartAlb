package com.smart.album.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.appcompat.widget.AppCompatImageView

class SlideImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var panningEndListener: OnPanningEndListener? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startPanningIfNecessary()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        startPanningIfNecessary()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val result = super.setFrame(l, t, r, b)
        startPanningIfNecessary()
        return result
    }

    private fun startPanningIfNecessary() {
        if (drawable != null && width > 0 && height > 0) {
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            Log.d("pan===", "width==$width")
            Log.d("pan===", "height==$height")
            Log.d("pan===", "drawableWidth==$drawableWidth")
            Log.d("pan===", "drawableHeight==$drawableHeight")
            if (drawableWidth > width || drawableHeight > height) {
                // 图片尺寸超过了屏幕大小
                panImage(drawableWidth, drawableHeight)
            }
        }
    }

    private fun panImage(drawableWidth: Int, drawableHeight: Int) {
        var fromXDelta = 0f
        var toXDelta = 0f
        var fromYDelta = 0f
        var toYDelta = 0f

        if (drawableWidth > width) {
            toXDelta = -width.toFloat()
        }

        if (drawableHeight > height) {
            toYDelta = -(drawableHeight - height).toFloat()
        }

        val animation = TranslateAnimation(
            fromXDelta, toXDelta,
            fromYDelta, toYDelta
        )

        Log.d("pan===", "fromXDelta==$fromXDelta")
        Log.d("pan===", "toXDelta==$toXDelta")
        Log.d("pan===", "fromYDelta==$fromYDelta")
        Log.d("pan===", "toYDelta==$toYDelta")
        animation.duration = 15000 // 动画持续时间
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                panningEndListener?.onPanningEnd()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        startAnimation(animation)
    }

    fun setOnPanningEndListener(listener: OnPanningEndListener) {
        this.panningEndListener = listener
    }

    interface OnPanningEndListener {
        fun onPanningEnd()
    }
}
