package com.smart.album.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var startMatrix: Matrix? = null
    private var endMatrix: Matrix? = null
    private var currentMatrix: Matrix? = null

    fun startZoomAnimation(duration: Long = 700) {
        // 保存当前矩阵
        startMatrix = imageMatrix

        // 设置初始状态为中心裁剪
        scaleType = ScaleType.CENTER_CROP
        invalidate()

        // 获取中心裁剪后的矩阵
        startMatrix = imageMatrix

        // 设置目标状态为适应中心
        scaleType = ScaleType.FIT_CENTER
        invalidate()

        // 获取适应中心后的矩阵
        endMatrix = imageMatrix

        // 重置为初始状态
        setImageMatrix(startMatrix)

        // 创建动画
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            setZoomProgress(progress)
        }
        animator.duration = duration
        animator.start()
    }

    private fun setZoomProgress(progress: Float) {
        if (startMatrix == null || endMatrix == null) return

        currentMatrix = Matrix()
        currentMatrix!!.set(startMatrix!!)

        val drawable = drawable ?: return
        val rectF = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())

        // 计算中间矩阵
        val tempMatrix = Matrix()
        tempMatrix.set(endMatrix!!)
        tempMatrix.postConcat(currentMatrix)

        // 应用插值
        currentMatrix!!.postConcat(tempMatrix)
        currentMatrix!!.setScale(progress, progress, rectF.centerX(), rectF.centerY())

        setImageMatrix(currentMatrix)
    }
}