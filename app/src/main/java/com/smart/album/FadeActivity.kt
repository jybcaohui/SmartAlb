package com.smart.album

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.viewpager2.widget.ViewPager2
import com.smart.album.adapters.ImagePagerAdapter
import com.smart.album.events.CloseCurrentEvent
import com.smart.album.events.ImageDisplayEvent
import com.smart.album.events.RefreshPageDataEvent
import com.smart.album.utils.PreferencesHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FadeActivity : BasePlayActivity() {
    private lateinit var viewPager: ViewPager2
    private var imageUrls: MutableList<String> = mutableListOf()
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var animationType = AnimationType.FADE
    private var adapter: ImagePagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNoTitle()
        setContentView(R.layout.activity_fade)
        setStatusBar()
        EventBus.getDefault().register(this)
        handler = Handler(Looper.getMainLooper())
        viewPager = findViewById(R.id.viewPager)
        adapter = ImagePagerAdapter(this, imageUrls)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false//禁止手动滑动
        // 处理边界情况
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                applyAnimation()
            }
        })
        initPageData()
    }

    private fun initPageData(){
        displaySeconds =  (PreferencesHelper.getInstance(this@FadeActivity).getInt(PreferencesHelper.DISPLAY_TIME_SECONDS,10)*1000).toLong()
        displayEffect =  PreferencesHelper.getInstance(this@FadeActivity).getInt(PreferencesHelper.DISPLAY_EFFECT,0)
        photoOrder =  PreferencesHelper.getInstance(this@FadeActivity).getInt(PreferencesHelper.PHOTO_ORDER,0)
        musicOn = PreferencesHelper.getInstance(this).getBoolean(PreferencesHelper.BG_MUSIC_ON,false)
        timerMinutes =  PreferencesHelper.getInstance(this@FadeActivity).getInt(PreferencesHelper.TIMER_MINUTES,0)

        imageUrls = getImagesFromFolder()
        adapter?.setNewData(imageUrls)
        // 设置自动滑动
        startAutoScroll()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RefreshPageDataEvent) {
        Log.d("event==",""+event.message)
        if(runnable != null){
            handler?.removeCallbacks(runnable!!)
        }
        initPageData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ImageDisplayEvent) {
        Log.d("event==",""+event.message)
        if(runnable != null){
            handler?.removeCallbacks(runnable!!)
        }
        initPageData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CloseCurrentEvent) {
        Log.d("event==",""+event.message)
        if(runnable != null){
            handler?.removeCallbacks(runnable!!)
        }
        finish()
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
        if(imageUrls.size > 1){
            runnable = Runnable {
                val currentPosition = viewPager.currentItem
                val nextPosition = (currentPosition + 1) % imageUrls.size
                viewPager.setCurrentItem(nextPosition, true)
                handler?.postDelayed(runnable!!, displaySeconds)
            }
            handler?.postDelayed(runnable!!, displaySeconds)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if(runnable != null){
            handler?.removeCallbacks(runnable!!)
        }
        EventBus.getDefault().unregister(this)
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

