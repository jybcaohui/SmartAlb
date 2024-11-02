package com.smart.album

import android.os.Bundle
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
import com.smart.album.CarouselActivity.AnimationType
import com.smart.album.adapters.SelectAdapter
import com.smart.album.utils.PreferencesHelper

class SettingActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var imgBack: ImageView
    private val displayTimeOptions = listOf("10 Seconds", "15 Seconds", "30 Seconds",
        "1 Minutes", "5 Minutes", "10 Minutes", "Custom:")

    private var displaySeconds:Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        rootLayout = findViewById(R.id.root)
        imgBack = findViewById(R.id.img_back)

        imgBack.setOnClickListener{
            finish()
        }

        findViewById<ConstraintLayout>(R.id.cl_display_time).setOnClickListener{
            showDisplayTimePop()
        }

    }
    private fun showDisplayTimePop() {
        displaySeconds =  PreferencesHelper.getInstance(this@SettingActivity).getInt(PreferencesHelper.DISPLAY_TIME_SECONDS,10)
        Log.d("TAG===", "displaySeconds: $displaySeconds")
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
            SelectAdapter(this, displayTimeOptions, selectedItemPosition, displaySeconds)
        } else {
            SelectAdapter(this, displayTimeOptions, selectedItemPosition, 0)
        }
        adapter.onItemSelectedListener = object : SelectAdapter.OnItemSelectedListener {
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
            Log.d("TAG===", "displaySeconds: $displaySeconds")
            PreferencesHelper.getInstance(this@SettingActivity).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,displaySeconds)
            popupWindow.dismiss()
        }
        popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0)
    }


}
