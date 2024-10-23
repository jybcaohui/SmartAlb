package com.smart.album.views

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import androidx.appcompat.widget.AppCompatImageView

class PanningImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var imageWidth = 0
    private var imageHeight = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var isHorizontalPan = false

    fun setImageSize(width: Int, height: Int) {
        imageWidth = width
        imageHeight = height
        screenWidth = this.width
        screenHeight = this.height

        isHorizontalPan = imageWidth * screenHeight > imageHeight * screenWidth

        if (isHorizontalPan) {
            val scale = screenHeight.toFloat() / imageHeight.toFloat()
            val matrix = Matrix()
            matrix.setScale(scale, scale)
            imageMatrix = matrix
        } else {
            val scale = screenWidth.toFloat() / imageWidth.toFloat()
            val matrix = Matrix()
            matrix.setScale(scale, scale)
            imageMatrix = matrix
        }

        startPanning()
    }

    private fun startPanning() {
        val anim = if (isHorizontalPan) {
            HorizontalPanAnimation()
        } else {
            VerticalPanAnimation()
        }
        anim.repeatCount = Animation.INFINITE
        anim.repeatMode = Animation.REVERSE
        anim.duration = 15000 // 15 seconds for one direction
        anim.interpolator = LinearInterpolator()
        startAnimation(anim)
    }

    private inner class HorizontalPanAnimation : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val matrix = t.matrix
            matrix.setTranslate(-(imageWidth - screenWidth) * interpolatedTime, 0f)
        }
    }

    private inner class VerticalPanAnimation : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val matrix = t.matrix
            matrix.setTranslate(0f, -(imageHeight - screenHeight) * interpolatedTime)
        }
    }
}
