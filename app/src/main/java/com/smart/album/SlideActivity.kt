package com.smart.album

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2
import com.smart.album.adapters.ImagePagerAdapter


class SlideActivity : BasePlayActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var imageUrls: List<String>
    private var handler: Handler? = null
    private var runnable: Runnable? = null

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
        viewPager.offscreenPageLimit = 3 // 预加载3个页面


        // 设置自动滑动
        startAutoScroll()

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


    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(runnable!!)
    }

}

