package com.smart.album.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.appcompat.widget.AppCompatImageView
import com.smart.album.utils.CommonUtil.pxToDp
import kotlin.math.abs
import kotlin.math.ceil

class PanningImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var onPanningEndListener: OnPanningEndListener? = null
    private var intrinsicWidth = 0
    private var intrinsicHeight = 0
    private var scale = 0.0f

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 获取图片的实际尺寸
        val drawable = drawable ?: return
        intrinsicWidth = drawable.intrinsicWidth
        intrinsicHeight = drawable.intrinsicHeight

        // 获取屏幕尺寸
        val viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        val viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        // 计算缩放比例
        val scaleX = viewWidth.toFloat() / intrinsicWidth
        val scaleY = viewHeight.toFloat() / intrinsicHeight
        scale = scaleX
        if (scaleX > scaleY){
            scale = scaleX
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            scale = scaleY
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        // 设置新的测量尺寸
//        setMeasuredDimension(viewWidth, viewHeight)
        setMeasuredDimension(layoutParams.width, layoutParams.height)

        // 缩放图片
        val matrix = imageMatrix
        matrix.setScale(scale, scale)
        setImageMatrix(matrix)

        // 启动平移动画
        startPanningIfNecessary(intrinsicWidth, intrinsicHeight, scale)
    }

    public fun startAnimat(): Boolean{
        //启动平移动画
        return startPanningIfNecessary(intrinsicWidth, intrinsicHeight, scale)
    }

    private fun startPanningIfNecessary(drawableWidth: Int, drawableHeight: Int, scale: Float): Boolean {
        val viewWidth = width
        val viewHeight = height

        val scaledDrawableWidth = drawableWidth * scale
        val scaledDrawableHeight = drawableHeight * scale

        var fromXDelta = 0f
        var toXDelta = 0f
        var fromYDelta = 0f
        var toYDelta = 0f

        if (scaledDrawableWidth > viewWidth) {
            toXDelta = (viewWidth - scaledDrawableWidth).toFloat()
        }

        if (scaledDrawableHeight > viewHeight) {
            toYDelta = (viewHeight - scaledDrawableHeight).toFloat()
        }

        if (toXDelta != 0f || toYDelta != 0f) {
            val animation = TranslateAnimation(
                fromXDelta, toXDelta,
                fromYDelta, toYDelta
            )
            var duration = 3000
            var distance = 0.0
            if(toXDelta != 0f){
                distance = context.pxToDp(abs(toXDelta)).toDouble()
            }
            if(toYDelta != 0f){
                distance = context.pxToDp(abs(toYDelta)).toDouble()
            }
            duration =  (ceil(distance / 30.0) * 1000).toInt()//控制移动速度，每秒移动30dp

            animation.duration = duration.toLong() // 动画持续时间
            animation.fillAfter = true
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    onPanningEndListener?.onPanningEnd()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            startAnimation(animation)
            return true
        } else {
            return false
        }

    }

    fun setOnPanningEndListener(listener: OnPanningEndListener) {
        this.onPanningEndListener = listener
    }


    interface OnPanningEndListener {
        fun onPanningEnd()
    }
}
