package com.smart.album

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import com.smart.album.utils.BlurBuilder
import com.smart.album.views.CrossFadeTransition
import com.smart.album.views.CrossFadeTransitionFactory
import com.smart.album.views.PanningImageView


class CrossFadeActivity : BasePlayActivity() {
    private lateinit var imageView: ImageView
    private lateinit var panImageView: PanningImageView
    private lateinit var imageUrls: List<String>
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNoTitle()
        setContentView(R.layout.activity_cross_fade)
        setStatusBar()

        imageView = findViewById(R.id.imageView)
        panImageView = findViewById(R.id.pan_img)



        // 图片 URL 列表
        imageUrls = listOf(
            "https://pic.616pic.com/bg_w1180/00/19/28/7gPY8D8pmb.jpg",
            "https://photocdn.sohu.com/20150826/mp29415155_1440604461249_2.jpg",
            "https://www.kuhw.com/d/file/p/2021/10-22/0d9525784ee4e7a74746eae20258bb79.jpg",
            "https://p5.itc.cn/q_70/images03/20221108/bc97e952dd2f4fa4a0a27402bcd8cad9.jpeg"
        )
        handler = Handler(Looper.getMainLooper())

        imageView.visibility = View.VISIBLE
        panImageView.visibility = View.GONE
        startAutoScroll()

//        imageView.visibility = View.GONE
//        panImageView.visibility = View.VISIBLE
//        startAutoScroll2()
    }


    private fun startAutoScroll() {
        val crossFadeFactory = DrawableCrossFadeFactory.Builder(2000).build() // 500ms的过渡时间
        runnable = Runnable {
            currentPosition ++
            if(currentPosition == imageUrls.size){
                currentPosition = 0
            }
            Log.d("cross===",""+currentPosition)
            Log.d("cross===",""+imageUrls[currentPosition])
            Glide.with(this)
                .asBitmap()
                .load(imageUrls[currentPosition])
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // 虚化背景
                        val blurredBackground = BlurBuilder.blur(this@CrossFadeActivity, resource)
                        // 将虚化的 Bitmap 设置为 ImageView 的背景
                        imageView.background = BitmapDrawable(resources, blurredBackground)
                        Glide.with(this@CrossFadeActivity)
                            .load(resource)
                            .transition(DrawableTransitionOptions.with(CrossFadeTransitionFactory(2000)))
                            .into(imageView)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 图片加载失败时的处理
                    }
                })
            handler?.postDelayed(runnable!!, 5000)
        }
        handler?.postDelayed(runnable!!, 500)
    }

    private fun startAutoScroll2() {
        val crossFadeFactory = DrawableCrossFadeFactory.Builder(2000).build() // 500ms的过渡时间
        runnable = Runnable {
            currentPosition ++
            if(currentPosition == imageUrls.size){
                currentPosition = 0
            }
            Log.d("cross===",""+currentPosition)
            Log.d("cross===",""+imageUrls[currentPosition])
            Glide.with(this@CrossFadeActivity)
                .load(imageUrls[currentPosition])
                .transition(DrawableTransitionOptions.withCrossFade(crossFadeFactory))
                .into(panImageView)
            handler?.postDelayed(runnable!!, 5000)
        }
        handler?.postDelayed(runnable!!, 500)
    }


    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(runnable!!)
    }

}

