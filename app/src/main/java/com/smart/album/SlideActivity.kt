package com.smart.album

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.smart.album.adapters.ImagePagerAdapter
import com.smart.album.events.CloseCurrentEvent
import com.smart.album.utils.PreferencesHelper
import com.smart.album.events.RefreshPageDataEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SlideActivity : BasePlayActivity() {
    private lateinit var viewPager: ViewPager2
    private var imageUrls: MutableList<String> = mutableListOf()
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var adapter: ImagePagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNoTitle()
        setContentView(R.layout.activity_fade)
        setStatusBar()
        EventBus.getDefault().register(this)
        handler = Handler(Looper.getMainLooper())
        viewPager = findViewById(R.id.viewPager)

        // 设置适配器
        adapter = ImagePagerAdapter(this, imageUrls)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false//禁止手动滑动
        viewPager.offscreenPageLimit = 3 // 预加载3个页面

        initPageData()
    }

    private fun initPageData(){
        displaySeconds =  (PreferencesHelper.getInstance(this@SlideActivity).getInt(PreferencesHelper.DISPLAY_TIME_SECONDS,10)*1000).toLong()
        displayEffect =  PreferencesHelper.getInstance(this@SlideActivity).getInt(PreferencesHelper.DISPLAY_EFFECT,0)
        photoOrder =  PreferencesHelper.getInstance(this@SlideActivity).getInt(PreferencesHelper.PHOTO_ORDER,0)
        musicOn = PreferencesHelper.getInstance(this).getBoolean(PreferencesHelper.BG_MUSIC_ON,false)
        timerMinutes =  PreferencesHelper.getInstance(this@SlideActivity).getInt(PreferencesHelper.TIMER_MINUTES,0)

        val spFileList = PreferencesHelper.getInstance(this).loadFileList()
        if(spFileList.isNotEmpty()){
            spFileList.forEach { file->
                imageUrls.add(imgDrivePath+file.id)
                Log.d("spFileList==","image==="+file.name)
            }
        } else {
            //默认图
            imageUrls = listOf(
                "https://p5.itc.cn/q_70/images03/20221108/bc97e952dd2f4fa4a0a27402bcd8cad9.jpeg"
            ).toMutableList()
        }
        adapter?.setNewData(imageUrls)
        // 设置自动滑动
        startAutoScroll()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RefreshPageDataEvent) {
        Log.d("event==",""+event.message)
        handler?.removeCallbacks(runnable!!)
        initPageData()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CloseCurrentEvent) {
        Log.d("event==",""+event.message)
        handler?.removeCallbacks(runnable!!)
        finish()
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
        handler?.removeCallbacks(runnable!!)
        EventBus.getDefault().unregister(this)
    }

}

