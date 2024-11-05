package com.smart.album

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.smart.album.adapters.TimerAdapter
import com.smart.album.adapters.TransitionEffectAdapter
import com.smart.album.utils.ImageDisplayEvent
import com.smart.album.utils.PreferencesHelper
import com.smart.album.utils.RefreshPageDataEvent
import com.smart.album.views.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class SettingActivity : BaseActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var imgBack: ImageView
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

    private val timerOptions = listOf(
        "Off",
        "5 Minutes",
        "15 Minutes",
        "30 Minutes",
        "1 hour",
        "2 hours",
        "Custom:")

    private var displaySeconds:Int = 0
    private var displayEffect:Int = 0
    private var transitionEffect:Int = 0
    private var photoOrder:Int = 0
    private var timerMinutes:Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        rootLayout = findViewById(R.id.root)
        imgBack = findViewById(R.id.img_back)
        imgMusic = findViewById(R.id.img_music)

        imgBack.setOnClickListener{
            finish()
        }
        var musicOn = PreferencesHelper.getInstance(this@SettingActivity).getBoolean(PreferencesHelper.BG_MUSIC_ON,false)
        if(musicOn){
            imgMusic.setImageResource(R.mipmap.ic_lock)
        } else {
            imgMusic.setImageResource(R.mipmap.ic_unlock)
        }
        imgMusic.setOnClickListener{
            musicOn = !musicOn
            PreferencesHelper.getInstance(this@SettingActivity).saveBoolean(PreferencesHelper.BG_MUSIC_ON,musicOn)
            if(musicOn){
                imgMusic.setImageResource(R.mipmap.ic_lock)
            } else {
                imgMusic.setImageResource(R.mipmap.ic_unlock)
            }
        }

        findViewById<ConstraintLayout>(R.id.cl_display_time).setOnClickListener{
            showDisplayTimePop()
        }
        findViewById<ConstraintLayout>(R.id.cl_display_effect).setOnClickListener{
            showDisplayEffectPop()
        }
        findViewById<ConstraintLayout>(R.id.cl_transition_effect).setOnClickListener{
            showTransitionEffectPop()
        }
        findViewById<ConstraintLayout>(R.id.cl_photo).setOnClickListener{
            showPhotoOrderPop()
        }
        findViewById<ConstraintLayout>(R.id.cl_schedules).setOnClickListener{
            startActivity(Intent(this, ScheduleSettingActivity::class.java))
        }
        findViewById<ConstraintLayout>(R.id.cl_timer).setOnClickListener{
            showTimerPop()
        }
        findViewById<ConstraintLayout>(R.id.cl_sync).setOnClickListener{
            showLoading()
            listFiles()
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
        }
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }

    private fun showTimerPop() {
        timerMinutes =  PreferencesHelper.getInstance(this@SettingActivity).getInt(PreferencesHelper.TIMER_MINUTES,0)
        Log.d("TAG===", "timerMinutes: $timerMinutes")
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
        val selectedItemPosition:Int = when (timerMinutes) {
            0 -> 0
            5 -> 1
            15 -> 2
            30 -> 3
            60 -> 4
            120 -> 5
            else -> 6
        }
        val adapter = if(selectedItemPosition == 6){
            TimerAdapter(this, timerOptions, selectedItemPosition, timerMinutes)
        } else {
            TimerAdapter(this, timerOptions, selectedItemPosition, 0)
        }
        adapter.onItemSelectedListener = object : TimerAdapter.OnItemSelectedListener {
            override fun onItemSelected(item: String, position: Int, minutes:Int) {
                when (position) {
                    0 -> timerMinutes = 0
                    1 -> timerMinutes = 5
                    2 -> timerMinutes = 15
                    3 -> timerMinutes = 30
                    4 -> timerMinutes = 60
                    5 -> timerMinutes = 120
                    6 -> timerMinutes = minutes
                }
            }
        }
        listView.adapter = adapter
        tvDone.setOnClickListener{
            PreferencesHelper.getInstance(this@SettingActivity).saveInt(PreferencesHelper.TIMER_MINUTES,timerMinutes)
            popupWindow.dismiss()
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
