package com.smart.album.utils

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

object BlurBuilder {

    private const val BITMAP_SCALE = 0.05f // 1/8
    private const val BLUR_RADIUS = 25f // 默认的模糊半径

    fun blur(context: Context, image: Bitmap): Bitmap {
        val width = (image.width * BITMAP_SCALE).toInt()
        val height = (image.height * BITMAP_SCALE).toInt()

        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)

        rs.destroy() // 释放RenderScript资源

        return outputBitmap
    }
}