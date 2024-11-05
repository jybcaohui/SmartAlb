package com.smart.album.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private var intrinsicWidth: Float = 0f
    private var intrinsicHeight: Float = 0f
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var startScale: Float = 0f
    private var endScale: Float = 0f


    init {
        scaleType = ScaleType.MATRIX
    }


    fun startZoomAnimation(duration: Long = 3000L) {
        // 计算中心点
        centerX = viewWidth / 2f
        centerY = viewHeight / 2f
        // 计算初始状态下的scale（CENTER_CROP）
        startScale = max(viewWidth / intrinsicWidth, viewHeight / intrinsicHeight)
        // 计算最终状态下的scale（FIT_CENTER）
        endScale = min(viewWidth / intrinsicWidth, viewHeight / intrinsicHeight)

        Log.d("ZoomingImageView", "startZoomAnimation=====")
        Log.d("ZoomingImageView", "start======= centerX: ${centerX} centerY: ${centerY}")
        Log.d("ZoomingImageView", "width = ${viewWidth}  height = ${viewHeight}")
        Log.d("ZoomingImageView", "intrinsicWidth = ${intrinsicWidth}  intrinsicHeight = ${intrinsicHeight}")
        Log.d("ZoomingImageView", "width/intrinsicHeight = ${viewWidth / intrinsicWidth} height/intrinsicHeight = ${viewHeight / intrinsicHeight}")
        Log.d("ZoomingImageView", "start======= startScale: ${startScale} endScale: ${endScale}")

        // 开始动画
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
//                        Log.d("ZoomingImageView", "===Animation update: ${progress}")
                updateMatrix(progress)
            }
        }
        animator.start()
        // 确保 ImageView 的大小已知
//        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                viewTreeObserver.removeOnGlobalLayoutListener(this)
//
////                // 获取图片的原始尺寸
////                val drawable = drawable ?: return
////                intrinsicWidth = drawable.intrinsicWidth.toFloat()
////                intrinsicHeight = drawable.intrinsicHeight.toFloat()
////                // 计算中心点
////                centerX = width / 2f
////                centerY = height / 2f
////
////                // 计算初始状态下的scale（CENTER_CROP）
////                startScale = max(width / intrinsicWidth, height / intrinsicHeight)
////                // 计算最终状态下的scale（FIT_CENTER）
////                endScale = min(width / intrinsicWidth, height / intrinsicHeight)
//
//                Log.d("ZoomingImageView", "start======= centerX: ${centerX} centerY: ${centerY}")
//                Log.d("ZoomingImageView", "width = ${width}  height = ${height}")
//                Log.d("ZoomingImageView", "intrinsicWidth = ${intrinsicWidth}  intrinsicHeight = ${intrinsicHeight}")
//                Log.d("ZoomingImageView", "width/intrinsicHeight = ${width / intrinsicWidth} height/intrinsicHeight = ${height / intrinsicHeight}")
//                Log.d("ZoomingImageView", "start======= startScale: ${startScale} endScale: ${endScale}")
//
//                // 开始动画
//                val animator = ValueAnimator.ofFloat(0f, 1f).apply {
//                    this.duration = duration
//                    addUpdateListener { animation ->
//                        val progress = animation.animatedValue as Float
////                        Log.d("ZoomingImageView", "===Animation update: ${progress}")
//                        updateMatrix(progress)
//                    }
//                }
//                animator.start()
//            }
//        })
    }

    private fun updateMatrix(progress: Float) {
        // 插值计算当前的scale
        val currentScale = startScale + (endScale - startScale) * progress
        // 重置并设置矩阵
        matrix.reset()
        matrix.postScale(currentScale, currentScale, centerX, centerY)
        // 应用新的矩阵
        imageMatrix = matrix
    }

    fun setIntrinsicSize(width: Int, height: Int) {
        intrinsicWidth = width.toFloat()
        intrinsicHeight = height.toFloat()
    }

    fun setScreenSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }
}