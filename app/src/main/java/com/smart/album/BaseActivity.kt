package com.smart.album

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs


open class BaseActivity : AppCompatActivity() {

    var autoScrollInterval: Long = 13000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    enum class AnimationType {
        FADE, SLIDE, CROSS_FADE
    }

    fun setNoTitle(){
        // 设置主题为无标题栏和全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

    }

    fun setStatusBar(){
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 隐藏状态栏和导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.navigationBars())
        }

        // 确保在API 21以上版本中应用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupTransparentStatusBar()
        }
    }

    private fun setupTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.apply {
            // 清除FLAG_TRANSLUCENT_STATUS flag
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            // 添加 FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag到window
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            // 更新系统UI可见性以保持沉浸模式
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            // 设置状态栏颜色为透明
            statusBarColor = Color.TRANSPARENT
        }
    }


    // 交叉淡入淡出页面转换器
    inner class CrossFadePageTransformer : ViewPager2.PageTransformer {
        private val MIN_SCALE = 0.2f // 滑出屏幕外时缩小的比例
        private val MIN_ALPHA = 0.1f  // 滑出屏幕外时透明度

        override fun transformPage(page: View, position: Float) {
            val pageWidth = page.width
            val pageHeight = page.height

            Log.d("trans===","position=="+position)

            when {
                position < -1 -> { // [-Infinity,-1)
                    // 这个页面已经完全离开了左边，不需要做任何处理。
                }
                position <= 1 -> { // [-1,1]
                    // 在当前页面和相邻页面之间应用交叉淡入淡出效果
                    val scaleFactor = maxOf(MIN_SCALE, 1 - abs(position))
                    val vertMargin = (pageHeight * (1 - scaleFactor)) / 2
                    val horzMargin = (pageWidth * (1 - scaleFactor)) / 2
                    if (position < 0) {
                        page.translationX = horzMargin - vertMargin / 2
                    } else {
                        page.translationX = -horzMargin + vertMargin / 2
                    }

                    // 设置透明度
                    page.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)

                    Log.d("trans===","alpha==="+page.alpha)
                    // 设置缩放
                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // 这个页面已经完全离开了右边，不需要做任何处理。
                }
            }
        }
    }

    inner class CrossFadePageTransformer2(private val dura: Long) : ViewPager2.PageTransformer {
        private val MIN_SCALE = 0.85f // 滑出屏幕外时缩小的比例
        private val MIN_ALPHA = 0.5f  // 滑出屏幕外时透明度

        override fun transformPage(page: View, position: Float) {
            val pageWidth = page.width
            val pageHeight = page.height

            Log.d("transformPage===","position==="+position)
            when {
                position < -1 -> { // [-Infinity,-1)
                    // 这个页面已经完全离开了左边，不需要做任何处理。
                }
                position <= 1 -> { // [-1,1]
                    // 在当前页面和相邻页面之间应用交叉淡入淡出效果
                    val scaleFactor = maxOf(MIN_SCALE, 1 - abs(position))
                    val vertMargin = (pageHeight * (1 - scaleFactor)) / 2
                    val horzMargin = (pageWidth * (1 - scaleFactor)) / 2
                    if (position < 0) {
                        page.translationX = horzMargin - vertMargin / 2
                    } else {
                        page.translationX = -horzMargin + vertMargin / 2
                    }

                    // 创建一个 ValueAnimator 来控制 alpha 和 scale 变化
                    ValueAnimator.ofFloat(0f, 1f).apply {
                        this.duration = dura
                        addUpdateListener { valueAnimator ->
                            val fraction = valueAnimator.animatedFraction
                            page.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA) * fraction
                            Log.d("transformPage===","alpha==="+page.alpha)
                            Log.d("transformPage===","scaleFactor==="+scaleFactor+"==fraction=="+fraction)
                            page.scaleX = scaleFactor * fraction
                            page.scaleY = scaleFactor * fraction
                            Log.d("transformPage===","scaleX==="+page.scaleX+"==scaleY=="+page.scaleY)
                        }
                        start()
                    }
                }
                else -> { // (1,+Infinity]
                    // 这个页面已经完全离开了右边，不需要做任何处理。
                }
            }
        }
    }
}

