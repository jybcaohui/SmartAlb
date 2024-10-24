package com.smart.album

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.viewpager2.widget.ViewPager2
import com.smart.album.adapters.ImagePagerAdapter


class FadeActivity : BaseActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var imageUrls: List<String>
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var autoScrollInterval: Long = 6000 // 3 seconds
    private var animationType = AnimationType.FADE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNoTitle()
        setContentView(R.layout.activity_fade)
        setStatusBar()

        handler = Handler(Looper.getMainLooper())
        viewPager = findViewById(R.id.viewPager)

        // 图片 URL 列表
        imageUrls = listOf(
            "https://pic.616pic.com/bg_w1180/00/19/28/7gPY8D8pmb.jpg",
            "https://photocdn.sohu.com/20150826/mp29415155_1440604461249_2.jpg",
            "https://www.kuhw.com/d/file/p/2021/10-22/0d9525784ee4e7a74746eae20258bb79.jpg",
            "https://p5.itc.cn/q_70/images03/20221108/bc97e952dd2f4fa4a0a27402bcd8cad9.jpeg"
        )
        // 设置适配器
        viewPager.adapter = ImagePagerAdapter(this, imageUrls)
        viewPager.isUserInputEnabled = false//禁止手动滑动


        // 设置自动滑动
        startAutoScroll()

        // 处理边界情况
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                applyAnimation()
//                scrollNext()
//                if (position == 0) {
//                    viewPager.setCurrentItem(imageUrls.size - 2, false)
//                } else if (position == imageUrls.size - 1) {
//                    viewPager.setCurrentItem(1, false)
//                }
            }
        })
    }

    private fun applyAnimation() {
        val animation = when (animationType) {
            AnimationType.FADE -> AnimationUtils.loadAnimation(this, R.anim.custom_fade_in)
            AnimationType.SLIDE -> AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
            AnimationType.CROSS_FADE -> AnimationUtils.loadAnimation(this, R.anim.cross_fade)
        }

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                viewPager.setPageTransformer(createPageTransformer())
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        viewPager.getChildAt(0)?.startAnimation(animation)
    }

    private fun startAutoScroll() {
        runnable = Runnable {
            val currentPosition = viewPager.currentItem
            val nextPosition = (currentPosition + 1) % imageUrls.size
            viewPager.setCurrentItem(nextPosition, true)
            handler?.postDelayed(runnable!!, autoScrollInterval)
        }
        handler?.postDelayed(runnable!!, autoScrollInterval)
    }

    private fun scrollNext(){
        // 延时3秒后执行任务
        handler?.postDelayed({
            val currentPosition = viewPager.currentItem
            val nextPosition = (currentPosition + 1) % imageUrls.size
            viewPager.setCurrentItem(nextPosition, true)
        }, 6000) // 3000 毫秒 = 3 秒

    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(runnable!!)
    }

    private fun createPageTransformer(): ViewPager2.PageTransformer {
        return ViewPager2.PageTransformer { page, position ->
            when (animationType) {
                AnimationType.FADE -> {
                    page.alpha = 1 - kotlin.math.abs(position)
                }
                AnimationType.SLIDE -> {
                    page.translationX = -position * page.width
                    if (position < -1f || position > 1f) {
                        page.alpha = 0f
                    } else if (position <= 0f || position <= 1f) {
                        page.alpha = 1f
                        page.pivotX = 0f
                        page.rotationY = 90f * kotlin.math.abs(position)
                    }
                }
                AnimationType.CROSS_FADE -> {
                    page.alpha = 1 - kotlin.math.abs(position)
                    page.scaleX = 1 - kotlin.math.abs(position)
                    page.scaleY = 1 - kotlin.math.abs(position)
                }
            }
        }
    }

}

