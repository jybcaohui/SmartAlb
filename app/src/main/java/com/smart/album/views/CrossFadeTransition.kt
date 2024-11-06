package com.smart.album.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.transition.Transition

class CrossFadeTransition(private val durationMillis: Long) : Transition<Drawable> {
    @SuppressLint("ObjectAnimatorBinding")
    override fun transition(new: Drawable?, adapter: Transition.ViewAdapter?): Boolean {
        if (adapter == null) {
            return false
        }

        new!!.alpha = 0 // 设置新图片初始透明度
        (adapter.view as? ImageView)?.setImageDrawable(new)
        adapter.view.tag = adapter.currentDrawable

        val fadeIn = ObjectAnimator.ofInt(new, "alpha", 0, 255)
        val fadeOut = ObjectAnimator.ofInt(adapter.currentDrawable, "alpha", 0, 255)

        fadeIn.duration = durationMillis
        fadeOut.duration = durationMillis

        val animatorSet = AnimatorSet().apply {
            playTogether(fadeIn, fadeOut)
            start()
        }

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                adapter.currentDrawable?.callback = null // 清理旧的Drawable
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
        return true
    }

}