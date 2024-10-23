package com.smart.album.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class AspectRatioImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var imageAspectRatio: Float = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (imageAspectRatio == 0f) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val screenAspectRatio = widthSize.toFloat() / heightSize.toFloat()

            if (imageAspectRatio > screenAspectRatio) {
                // 图片宽高比大于屏幕宽高比，高度占满屏幕
                val width = (heightSize * imageAspectRatio).toInt()
                setMeasuredDimension(width, heightSize)
            } else {
                // 图片宽高比小于或等于屏幕宽高比，宽度占满屏幕
                val height = (widthSize / imageAspectRatio).toInt()
                setMeasuredDimension(widthSize, height)
            }
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null) {
            imageAspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
            requestLayout()
        }
    }
}
