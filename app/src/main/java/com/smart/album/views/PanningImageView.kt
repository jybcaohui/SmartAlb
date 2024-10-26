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
    private var viewWidth = 0//初始控件宽高MATCH_PARENT
    private var viewHeight = 0

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
        if (scaleX > scaleY){
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        // 设置新的测量尺寸
        setMeasuredDimension(layoutParams.width, layoutParams.height)

        // 启动平移动画
        startPanningIfNecessary(intrinsicWidth, intrinsicHeight)
    }


    private fun startPanningIfNecessary(drawableWidth: Int, drawableHeight: Int): Boolean {
        if(viewWidth == 0 || viewHeight == 0){
            viewWidth = width
            viewHeight = height
        }

        val fromXDelta = 0f
        var toXDelta = 0f
        val fromYDelta = 0f
        var toYDelta = 0f

        if (drawableWidth > viewWidth) {
            toXDelta = (viewWidth - drawableWidth).toFloat()
        }

        if (drawableHeight > viewHeight) {
            toYDelta = (viewHeight - drawableHeight).toFloat()
        }

        if (toXDelta != 0f || toYDelta != 0f) {
            animationImage(viewWidth,viewHeight,fromXDelta,toXDelta,fromYDelta,toYDelta)
            return true
        } else {
            return false
        }

    }


    private fun animationImage(viewWidth:Int,viewHeight:Int,fromXDelta:Float, toXDelta:Float, fromYDelta:Float, toYDelta:Float){
        val animation = TranslateAnimation(
            fromXDelta, toXDelta,
            fromYDelta, toYDelta
        )
        var duration = 3000
        var distance = 0.0

        if(fromXDelta != 0f || toXDelta != 0f){
            distance = context.pxToDp(abs(toXDelta-fromXDelta)).toDouble()
            duration =  (ceil(distance / 20.0) * 1000).toInt()//控制横向移动速度，每秒移动30dp
        }

        if(fromYDelta != 0f || toYDelta != 0f){
            distance =  context.pxToDp(abs(toYDelta-fromYDelta)).toDouble()
            duration =  (ceil(distance / 40.0) * 1000).toInt()//控制纵向移动速度，每秒移动60dp
        }

        animation.duration = duration.toLong() // 动画持续时间
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
//                onPanningEndListener?.onPanningEnd()
//                Thread.sleep(100) // 1000 毫秒 = 1 秒
                animationImage(viewWidth,viewHeight,toXDelta,fromXDelta,toYDelta,fromYDelta)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        startAnimation(animation)
    }

    fun setOnPanningEndListener(listener: OnPanningEndListener) {
        this.onPanningEndListener = listener
    }


    interface OnPanningEndListener {
        fun onPanningEnd()
    }
}
