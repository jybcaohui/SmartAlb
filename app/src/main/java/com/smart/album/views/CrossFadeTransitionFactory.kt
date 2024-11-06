package com.smart.album.views

import android.graphics.drawable.Drawable
import android.view.View
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

class CrossFadeTransitionFactory(private val durationMillis: Long) : TransitionFactory<Drawable> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CrossFadeTransitionFactory

        if (durationMillis != other.durationMillis) return false

        return true
    }

    override fun hashCode(): Int {
        return durationMillis.hashCode()
    }

    override fun build(dataSource: DataSource?, isFirstResource: Boolean): Transition<Drawable> {
        return CrossFadeTransition(durationMillis)
    }
}