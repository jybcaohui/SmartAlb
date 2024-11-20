package com.smart.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.smart.album.events.ImageDisplayEvent
import com.smart.album.utils.PreferencesHelper
import org.greenrobot.eventbus.EventBus

class SettingDisplayEffectActivity : BaseActivity() {

    private var displayEffect:Int = 0
    private lateinit var imgBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var lvPan: LinearLayout
    private lateinit var lvScale: LinearLayout
    private lateinit var lvCrop: LinearLayout
    private lateinit var lvZoom: LinearLayout
    private lateinit var lvFocus: LinearLayout


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_display_effect)
        imgBack = findViewById(R.id.img_back)
        tvTitle = findViewById(R.id.tv_title)
        tvTitle.text = getString(R.string.setting)
        imgBack.setOnClickListener{
            finish()
        }

        displayEffect =  PreferencesHelper.getInstance(this).getInt(PreferencesHelper.DISPLAY_EFFECT,0)

        lvPan = findViewById(R.id.lv_pan)
        lvScale = findViewById(R.id.lv_scale)
        lvCrop = findViewById(R.id.lv_crop)
        lvZoom = findViewById(R.id.lv_zoom)
        lvFocus = findViewById(R.id.lv_focus)

        when (displayEffect) {
            0 -> lvPan.requestFocus()
            1 -> lvScale.requestFocus()
            2 -> lvCrop.requestFocus()
            3 -> lvZoom.requestFocus()
            4 -> lvFocus.requestFocus()
            else -> lvPan.requestFocus()

        }

        lvPan.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_EFFECT,0)
            EventBus.getDefault().post(ImageDisplayEvent("ImageDisplay"))
            finish()
        }
        lvScale.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_EFFECT,1)
            EventBus.getDefault().post(ImageDisplayEvent("ImageDisplay"))
            finish()
        }
        lvCrop.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_EFFECT,2)
            EventBus.getDefault().post(ImageDisplayEvent("ImageDisplay"))
            finish()
        }
        lvZoom.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_EFFECT,3)
            EventBus.getDefault().post(ImageDisplayEvent("ImageDisplay"))
            finish()
        }
        lvFocus.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_EFFECT,4)
            EventBus.getDefault().post(ImageDisplayEvent("ImageDisplay"))
            finish()
        }

    }




}
