package com.smart.album.utils

import android.content.Context
import android.util.DisplayMetrics

object CommonUtil {
    fun Context.pxToDp(px: Float): Float {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return px / displayMetrics.density
    }
}