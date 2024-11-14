package com.smart.album

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smart.album.utils.PreferencesHelper
import com.smart.album.views.LoadingDialog
import org.apache.http.util.TextUtils
import kotlin.math.abs


open class BasePlayActivity : AppCompatActivity() {


    var displaySeconds: Long = 10000 //轮播间隔时间（毫秒，10s，最低5s）
    var displayEffect:Int = 0 //图片裁剪样式
    var transitionEffect:Int = 0 //图片切换动效
    var photoOrder:Int = 0 //文件排序
    var timerMinutes: Int = 0 //运行时间（单位：分钟）
    var musicOn = false//背景音乐开启（否）


    private var mediaPlayer: MediaPlayer? = null
    private var loadingDialog: LoadingDialog? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog()
        displaySeconds =  (PreferencesHelper.getInstance(this@BasePlayActivity).getInt(PreferencesHelper.DISPLAY_TIME_SECONDS,10)*1000).toLong()
        displayEffect =  PreferencesHelper.getInstance(this@BasePlayActivity).getInt(PreferencesHelper.DISPLAY_EFFECT,0)
        transitionEffect =  PreferencesHelper.getInstance(this@BasePlayActivity).getInt(PreferencesHelper.TRANSITION_EFFECT,0)
        photoOrder =  PreferencesHelper.getInstance(this@BasePlayActivity).getInt(PreferencesHelper.PHOTO_ORDER,0)
        musicOn = PreferencesHelper.getInstance(this).getBoolean(PreferencesHelper.BG_MUSIC_ON,false)
        timerMinutes =  PreferencesHelper.getInstance(this@BasePlayActivity).getInt(PreferencesHelper.TIMER_MINUTES,0)

        if(displaySeconds < 5000){
            displaySeconds = 5000
        }

        val imageUrls = getImagesFromFolder()
        if (imageUrls.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 根布局点击事件
        window.decorView.findViewById<View>(android.R.id.content)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 添加到活动列表
        App.instance.addActivity(this)
    }

    private fun initMusic(){
        // 初始化 MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.bensound_happiness)
        mediaPlayer?.isLooping = true // 设置循环播放
        mediaPlayer?.start()
    }

    override fun onResume() {
        super.onResume()
        musicOn = PreferencesHelper.getInstance(this).getBoolean(PreferencesHelper.BG_MUSIC_ON,false)
        if(musicOn){
            if(mediaPlayer == null){
                initMusic()
            } else {
                if (!mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.start()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 暂停播放
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.stop()
        mediaPlayer?.release()
        // 从活动列表中移除
        App.instance.removeActivity(this)
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
        // 确保在API 21以上版本中应用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupTransparentStatusBar()
        }
        // 隐藏底部导航栏
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.insetsController?.hide(WindowInsets.Type.navigationBars())
//        }


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
                            page.scaleX = scaleFactor * fraction
                            page.scaleY = scaleFactor * fraction
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity(); // 关闭所有属于当前任务的Activity
//        exitProcess(0); // 结束整个进程
    }


    fun getImagesFromFolder(): MutableList<String> {
        val images = mutableListOf<String>()
        val localUriStr = PreferencesHelper.getInstance(this).getStr(PreferencesHelper.LOCAL_FOLDER_URI)
        if(TextUtils.isEmpty(localUriStr)){
            return images
        }
        val pickedDir = DocumentFile.fromTreeUri(this, Uri.parse(localUriStr))
        pickedDir?.listFiles()?.forEach { file ->
            if (file.isFile) {
                val name = file.name ?: ""
                if (name.lowercase().endsWith(".jpg") || name.lowercase().endsWith(".jpeg") || name.lowercase().endsWith(".png")) {
                    images.add(file.uri.toString())//file.uri
                }
            }
        }
        return images
    }
}

