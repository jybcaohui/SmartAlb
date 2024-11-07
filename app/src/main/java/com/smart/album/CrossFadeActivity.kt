package com.smart.album

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.smart.album.utils.BlurBuilder
import com.smart.album.utils.PreferencesHelper
import com.smart.album.views.PanningImageView
import kotlin.math.max


class CrossFadeActivity : BasePlayActivity() {
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var panImageView1: PanningImageView
    private lateinit var panImageView2: PanningImageView
    private lateinit var imageUrls: List<String>
    private lateinit var handler: Handler
    private var currentIndex = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNoTitle()
        setContentView(R.layout.activity_cross_fade)
        setStatusBar()


        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        panImageView1 = findViewById(R.id.panImageView1)
        panImageView2 = findViewById(R.id.panImageView2)
        displayEffect =  PreferencesHelper.getInstance(this).getInt(PreferencesHelper.DISPLAY_EFFECT,0)
        when(displayEffect){
            0->{
                //panImageView
                imageView1.visibility = View.GONE
                imageView1.visibility = View.GONE
                panImageView1.visibility = View.VISIBLE
                panImageView2.visibility = View.VISIBLE
            }
            1->{
                //fitCenter
                imageView1.visibility = View.VISIBLE
                imageView1.visibility = View.VISIBLE
                panImageView1.visibility = View.GONE
                panImageView2.visibility = View.GONE
                imageView1.scaleType = ImageView.ScaleType.FIT_CENTER
                imageView2.scaleType = ImageView.ScaleType.FIT_CENTER
            }
            2->{
                //centerCrop
                imageView1.visibility = View.VISIBLE
                imageView1.visibility = View.VISIBLE
                panImageView1.visibility = View.GONE
                panImageView2.visibility = View.GONE
                imageView1.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView2.scaleType = ImageView.ScaleType.CENTER_CROP
            }
            3,4->{
                //zoom
                imageView1.visibility = View.VISIBLE
                imageView1.visibility = View.VISIBLE
                panImageView1.visibility = View.GONE
                panImageView2.visibility = View.GONE
            }
        }
        // 图片 URL 列表
        imageUrls = listOf(
            "https://pic.616pic.com/bg_w1180/00/19/28/7gPY8D8pmb.jpg",
            "https://photocdn.sohu.com/20150826/mp29415155_1440604461249_2.jpg",
            "https://www.kuhw.com/d/file/p/2021/10-22/0d9525784ee4e7a74746eae20258bb79.jpg",
            "https://p5.itc.cn/q_70/images03/20221108/bc97e952dd2f4fa4a0a27402bcd8cad9.jpeg"
        )
        handler = Handler(Looper.getMainLooper())

        // 加载第一张图片
        loadNextImage()

        // 启动定时器
        startSlideshow()
    }

    private fun loadNextImage() {
        // 更新索引
        currentIndex = (currentIndex + 1) % imageUrls.size

        if(displayEffect == 0){
            val currentImageView = if (currentIndex % 2 == 0) panImageView1 else panImageView2
            val nextImageView = if (currentIndex % 2 == 0) panImageView2 else panImageView1
//            nextImageView.requestLayout()
//            nextImageView.invalidate()
//            Glide.with(this)
//                .load(imageUrls[currentIndex])
//                .into(nextImageView)
            Glide.with(this)
                .asBitmap()
                .load(imageUrls[currentIndex])
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // 切换到另一张图片
                        nextImageView.setImageBitmap(resource)
                        nextImageView.requestLayout()
                        nextImageView.invalidate()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 图片加载失败时的处理
                    }
                })
            // 开始淡入淡出动画
            crossFade(currentImageView, nextImageView)
        } else {
            val currentImageView = if (currentIndex % 2 == 0) imageView1 else imageView2
            val nextImageView = if (currentIndex % 2 == 0) imageView2 else imageView1
            Glide.with(this)
                .asBitmap()
                .load(imageUrls[currentIndex])
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // 虚化背景
                        val blurredBackground = BlurBuilder.blur(this@CrossFadeActivity, resource)
                        nextImageView.background = BitmapDrawable(resources, blurredBackground)
                        // 切换到另一张图片
                        nextImageView.setImageDrawable(BitmapDrawable(resources,resource))

                        if(displayEffect == 3 || displayEffect == 4){
                            //Zoom 动画
                            val viewWidth = resources.displayMetrics.widthPixels
                            val viewHeight = resources.displayMetrics.heightPixels
                            // 计算图片初始尺寸
                            val imageWidth = resource.width
                            val imageHeight = resource.height

                            // 计算初始缩放比例
                            var initialScale = 1.0f
                            var finalScale = if(viewWidth > imageWidth && viewHeight > imageHeight){
                                max(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
                            } else {
                                max( imageWidth / viewWidth.toFloat(), imageHeight / viewHeight.toFloat())
                            }
                            if(finalScale < 3.0f){
                                finalScale = 3.0f
                            }
                            nextImageView.scaleX = initialScale
                            nextImageView.scaleY = initialScale
                            val scaleXAnimator = ObjectAnimator.ofFloat(nextImageView, "scaleX", finalScale,initialScale)
                            val scaleYAnimator = ObjectAnimator.ofFloat(nextImageView, "scaleY", finalScale,initialScale)

                            val animatorSet = AnimatorSet()
                            // 将动画添加到AnimatorSet中
                            animatorSet.playTogether(scaleXAnimator, scaleYAnimator)
                            animatorSet.duration = 3000  // 动画持续时间2秒
                            // 开始动画
                            animatorSet.start()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 图片加载失败时的处理
                    }
                })
            // 开始淡入淡出动画
            crossFade(currentImageView, nextImageView)
        }

    }

    private fun crossFade(currentImageView: ImageView, nextImageView: ImageView) {
        // 淡出当前图片
        ObjectAnimator.ofFloat(currentImageView, "alpha", 1f, 0f)
            .setDuration(2000)
            .start()

        // 淡入下一张图片
        nextImageView.alpha = 0f
        ObjectAnimator.ofFloat(nextImageView, "alpha", 0f, 1f)
            .setDuration(2000)
            .start()
    }

    private fun startSlideshow() {
        handler.postDelayed({
            loadNextImage()
            startSlideshow() // 递归调用，实现无限循环
        }, displaySeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清除 Handler 中的所有回调
        handler.removeCallbacksAndMessages(null)
    }
}

