package com.smart.album

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.smart.album.adapters.CarouselAdapter
import com.smart.album.models.CarouselItem

class CarouselActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: CarouselAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private var animationType = AnimationType.FADE

    private val carouselItems = listOf(
        CarouselItem("https://pic.616pic.com/bg_w1180/00/19/28/7gPY8D8pmb.jpg", "Image 0"),
        CarouselItem("https://photocdn.sohu.com/20150826/mp29415155_1440604461249_2.jpg", "Image 1"),
        CarouselItem("https://file.nbfox.com/wp-content/uploads/2020/04/1665_Girl_with_a_Pearl_Earring_nbfox.jpg", "Image 2"),
        CarouselItem("https://www.kuhw.com/d/file/p/2021/10-22/0d9525784ee4e7a74746eae20258bb79.jpg", "Image 3"),
        CarouselItem("https://p5.itc.cn/q_70/images03/20221108/bc97e952dd2f4fa4a0a27402bcd8cad9.jpeg", "Image 4"),
        CarouselItem("https://k.sinaimg.cn/n/collect/crawl/20160224/HK7h-fxprucu3187034.jpg/w700d1q75cms.jpg", "Image 5")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carousel)

        viewPager = findViewById(R.id.viewPager)
        adapter = CarouselAdapter(carouselItems)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1 // 预加载3个页面

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                applyAnimation()

                adapter.showAnimat()
            }
        })

        startAutoScroll()
    }

    private fun startAutoScroll() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (currentPage == carouselItems.size) {
                    currentPage = 0
                }
                viewPager.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, 3000)
            }
        }, 3000)
    }

    fun changeAnimation(view: View) {
        animationType = when (animationType) {
            AnimationType.FADE -> AnimationType.SLIDE
            AnimationType.SLIDE -> AnimationType.CROSS_FADE
            AnimationType.CROSS_FADE -> AnimationType.FADE
        }
        applyAnimation()
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

    enum class AnimationType {
        FADE, SLIDE, CROSS_FADE
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
