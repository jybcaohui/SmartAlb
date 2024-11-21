package com.smart.album

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.smart.album.adapters.DisplayEffectAdapter
import com.smart.album.adapters.DisplayTimeAdapter
import com.smart.album.adapters.PhotoOrderAdapter
import com.smart.album.adapters.TransitionEffectAdapter
import com.smart.album.events.CloseCurrentEvent
import com.smart.album.events.ImageDisplayEvent
import com.smart.album.events.RefreshPageDataEvent
import com.smart.album.utils.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus


class SettingActivity : BaseActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var imgBack: ImageView
    private lateinit var img_h_check: ImageView
    private lateinit var img_v_check: ImageView
    private lateinit var clDisplayTime: ConstraintLayout
    private lateinit var clDisplayEffect: ConstraintLayout
    private lateinit var clTransitionEffect: ConstraintLayout
    private lateinit var clSchedules: ConstraintLayout
    private lateinit var clTimer: ConstraintLayout
    private lateinit var clSync: ConstraintLayout
    private lateinit var clScreenH: ConstraintLayout
    private lateinit var clScreenV: ConstraintLayout

    private lateinit var clMusic: ConstraintLayout
    private lateinit var imgMusic: ImageView
    private var driveService: Drive? = null

    private val displayTimeOptions = listOf(
        "10 Seconds",
        "15 Seconds",
        "30 Seconds",
        "1 Minutes",
        "5 Minutes",
        "10 Minutes",
        "Custom:")
    private val displayEffectOptions = listOf(
        "Pan",
        "Scale to Fit Center",
        "Crop to Fit Center",
        "Zoom",
        "Focus")
    private val transitionEffectOptions = listOf(
        "Fade",
        "Cross Fade",
        "Memory")
    private val photoOrderOptions = listOf(
        "Shuffle",
        "Random",
        "Flle Name (A to Z)",
        "File Name (Z to A)",
        "Modified Date (Earliest first)",
        "Modified Date (Latest first)")


    private var displaySeconds:Int = 0
    private var displayEffect:Int = 0
    private var transitionEffect:Int = 0
    private var photoOrder:Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        rootLayout = findViewById(R.id.root)
        imgBack = findViewById(R.id.img_back)
        imgMusic = findViewById(R.id.img_music)
        clMusic = findViewById(R.id.cl_music)

        imgBack.setOnClickListener{
            finish()
        }

        clDisplayTime = findViewById(R.id.cl_display_time)
        clDisplayTime.requestFocus()
        clDisplayTime.setOnClickListener{
//            showDisplayTimePop()
            startActivity(Intent(this, SettingDisplayTimeActivity::class.java))
        }
        clDisplayEffect=findViewById(R.id.cl_display_effect)
        clDisplayEffect.setOnClickListener{
//            showDisplayEffectPop()
            startActivity(Intent(this, SettingDisplayEffectActivity::class.java))
        }
        clTransitionEffect = findViewById(R.id.cl_transition_effect)
        clTransitionEffect.setOnClickListener{
//            showTransitionEffectPop()
            startActivity(Intent(this, SettingTransitionEffectActivity::class.java))
        }
        clSchedules = findViewById(R.id.cl_schedules)
        clSchedules.setOnClickListener{
            startActivity(Intent(this, SettingScheduleActivity::class.java))
        }
        clTimer = findViewById(R.id.cl_timer)
        clTimer.setOnClickListener{
            startActivity(Intent(this, SettingTimerActivity::class.java))
        }
//        clSync = findViewById(R.id.cl_sync)
//        clSync.setOnClickListener{
//            showLoading()
//            listFiles()
//        }

        img_h_check = findViewById(R.id.img_h_check)
        img_v_check = findViewById(R.id.img_v_check)
        if(PreferencesHelper.getInstance(this).getInt(PreferencesHelper.SCREEN_ORIENTATION,0)==0){
            img_h_check.visibility = View.VISIBLE
            img_v_check.visibility = View.GONE
        } else {
            img_h_check.visibility = View.GONE
            img_v_check.visibility = View.VISIBLE
        }
        clScreenH = findViewById(R.id.cl_screen_h)
        clScreenH.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.SCREEN_ORIENTATION,0)
            EventBus.getDefault().post(CloseCurrentEvent("close playing activity"))
            startActivity(Intent(this@SettingActivity,WelcomeActivity::class.java))
            finish()
        }

        clScreenV = findViewById(R.id.cl_screen_v)
        clScreenV.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.SCREEN_ORIENTATION,1)
            EventBus.getDefault().post(CloseCurrentEvent("close playing activity"))
            startActivity(Intent(this@SettingActivity,WelcomeActivity::class.java))
            finish()
        }

        var musicOn = PreferencesHelper.getInstance(this@SettingActivity).getBoolean(PreferencesHelper.BG_MUSIC_ON,false)
        if(musicOn){
            imgMusic.setImageResource(R.mipmap.ic_lock)
        } else {
            imgMusic.setImageResource(R.mipmap.ic_unlock)
        }
        clMusic.setOnClickListener{
            musicOn = !musicOn
            PreferencesHelper.getInstance(this@SettingActivity).saveBoolean(PreferencesHelper.BG_MUSIC_ON,musicOn)
            if(musicOn){
                imgMusic.setImageResource(R.mipmap.ic_lock)
            } else {
                imgMusic.setImageResource(R.mipmap.ic_unlock)
            }
        }

    }
    private fun showDisplayTimePop() {
        displaySeconds =  PreferencesHelper.getInstance(this@SettingActivity).getInt(PreferencesHelper.DISPLAY_TIME_SECONDS,10)
        val popupView = layoutInflater.inflate(R.layout.bottom_pop, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        popupView.findViewById<LinearLayout>(R.id.lv_root).setOnClickListener { popupWindow.dismiss() }
        val listView = popupView.findViewById<ListView>(R.id.listView)


        listView.requestFocus()
        // 设置方向键监听器
        listView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // 上方向键
                        val currentPosition = listView.selectedItemPosition
                        if (currentPosition > 0) {
                            listView.setSelection(currentPosition - 1)
                        }
                        return@setOnKeyListener true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // 下方向键
                        val currentPosition = listView.selectedItemPosition
                        if (currentPosition < listView.count - 1) {
                            listView.setSelection(currentPosition + 1)
                        }
                        return@setOnKeyListener true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        // 中心选择键或回车键
                        val currentPosition = listView.selectedItemPosition
                        Log.d("FocusTest", "Item clicked at position: $currentPosition")
                        // 处理选中项的操作
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }

        val tvDone = popupView.findViewById<TextView>(R.id.tv_done)
        val selectedItemPosition:Int = when (displaySeconds) {
            10 -> 0
            15 -> 1
            30 -> 2
            60 -> 3
            300 -> 4
            600 -> 5
            else -> 6
        }
        val adapter = if(selectedItemPosition == 6){
            DisplayTimeAdapter(this, displayTimeOptions, selectedItemPosition, displaySeconds)
        } else {
            DisplayTimeAdapter(this, displayTimeOptions, selectedItemPosition, 0)
        }
        adapter.onItemSelectedListener = object : DisplayTimeAdapter.OnItemSelectedListener {
            override fun onItemSelected(item: String, position: Int, seconds:Int) {
                when (position) {
                    0 -> displaySeconds = 10 //10 Seconds
                    1 -> displaySeconds = 15 //15 Seconds
                    2 -> displaySeconds = 30 //30 Seconds
                    3 -> displaySeconds = 60 //1 Minutes
                    4 -> displaySeconds = 300 //5 Minutes
                    5 -> displaySeconds = 600 //10 Minutes
                    6 -> displaySeconds = seconds
                }
            }
        }
        listView.adapter = adapter
        tvDone.setOnClickListener{
            PreferencesHelper.getInstance(this@SettingActivity).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,displaySeconds)
            popupWindow.dismiss()

            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
        }
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }

    private fun showDisplayEffectPop() {
        displayEffect =  PreferencesHelper.getInstance(this@SettingActivity).getInt(PreferencesHelper.DISPLAY_EFFECT,0)
        val popupView = layoutInflater.inflate(R.layout.bottom_pop, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        popupView.findViewById<LinearLayout>(R.id.lv_root).setOnClickListener { popupWindow.dismiss() }
        val listView = popupView.findViewById<ListView>(R.id.listView)
        val tvDone = popupView.findViewById<TextView>(R.id.tv_done)
        val selectedItemPosition:Int = displayEffect
        val adapter = DisplayEffectAdapter(this, displayEffectOptions, selectedItemPosition)

        adapter.onItemSelectedListener = object : DisplayEffectAdapter.OnItemSelectedListener {
            override fun onItemSelected(item: String, position: Int) {
                displayEffect = position
            }
        }
        listView.adapter = adapter
        tvDone.setOnClickListener{
            PreferencesHelper.getInstance(this@SettingActivity).saveInt(PreferencesHelper.DISPLAY_EFFECT,displayEffect)
            popupWindow.dismiss()

            EventBus.getDefault().post(ImageDisplayEvent("ImageDisplay"))
        }
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }

    private fun showTransitionEffectPop() {
        transitionEffect =  PreferencesHelper.getInstance(this@SettingActivity).getInt(PreferencesHelper.TRANSITION_EFFECT,0)
        val popupView = layoutInflater.inflate(R.layout.bottom_pop, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        popupView.findViewById<LinearLayout>(R.id.lv_root).setOnClickListener { popupWindow.dismiss() }
        val listView = popupView.findViewById<ListView>(R.id.listView)
        val tvDone = popupView.findViewById<TextView>(R.id.tv_done)
        val selectedItemPosition:Int = transitionEffect
        val adapter = TransitionEffectAdapter(this, transitionEffectOptions, selectedItemPosition)

        adapter.onItemSelectedListener = object : TransitionEffectAdapter.OnItemSelectedListener {
            override fun onItemSelected(item: String, position: Int) {
                transitionEffect = position
            }
        }
        listView.adapter = adapter
        tvDone.setOnClickListener{
            PreferencesHelper.getInstance(this@SettingActivity).saveInt(PreferencesHelper.TRANSITION_EFFECT,transitionEffect)
            popupWindow.dismiss()
            EventBus.getDefault().post(CloseCurrentEvent("close playing activity"))
            startActivity(Intent(this@SettingActivity,WelcomeActivity::class.java))
        }
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }


    private fun showPhotoOrderPop() {
        photoOrder =  PreferencesHelper.getInstance(this@SettingActivity).getInt(PreferencesHelper.PHOTO_ORDER,0)
        val popupView = layoutInflater.inflate(R.layout.bottom_pop, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        popupView.findViewById<LinearLayout>(R.id.lv_root).setOnClickListener { popupWindow.dismiss() }
        val listView = popupView.findViewById<ListView>(R.id.listView)
        val tvDone = popupView.findViewById<TextView>(R.id.tv_done)
        val selectedItemPosition:Int = photoOrder
        val adapter = PhotoOrderAdapter(this, photoOrderOptions, selectedItemPosition)

        adapter.onItemSelectedListener = object : PhotoOrderAdapter.OnItemSelectedListener {
            override fun onItemSelected(item: String, position: Int) {
                photoOrder = position
            }
        }
        listView.adapter = adapter
        tvDone.setOnClickListener{
            PreferencesHelper.getInstance(this@SettingActivity).saveInt(PreferencesHelper.PHOTO_ORDER,photoOrder)
            popupWindow.dismiss()
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
        }
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }

    private fun setupDriveService() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account != null){
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_READONLY)
            )
            credential.selectedAccount = account.account
            driveService = Drive.Builder(
                NetHttpTransport(),  // 使用 NetHttpTransport 替代 AndroidHttp
                GsonFactory(),
                credential
            )
                .setApplicationName("Smart Album")
                .build()
        }
    }
    private fun listFiles() {
        setupDriveService()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                var query = "mimeType='image/jpeg' or mimeType='image/png'"
                val folderId = PreferencesHelper.getInstance(this@SettingActivity).getStr(PreferencesHelper.DRIVE_FOLDER_ID)
                if(!TextUtils.isEmpty(folderId)){
                    query = "mimeType='image/jpeg' or mimeType='image/png' and '$folderId' in parents and trashed=false"
                }
                Log.d("albs===","query="+query)
                val files = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setPageSize(30)
                        ?.setQ(query)
                        ?.setFields("files(id, name, mimeType)")
//                        ?.setFields("files(id, name, mimeType, modifiedTime)")
                        ?.execute()
                        ?.files ?: emptyList()
                }
                // 保存列表
                PreferencesHelper.getInstance(this@SettingActivity).saveFileList(files)
                hideLoading()
                Toast.makeText(this@SettingActivity, "Sync successfully. Find ${files.size} Images", Toast.LENGTH_SHORT).show()
                EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            } catch (e: Exception) {
            }
        }
    }


}
