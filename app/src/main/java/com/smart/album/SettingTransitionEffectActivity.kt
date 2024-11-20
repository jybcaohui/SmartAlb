package com.smart.album

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.smart.album.events.CloseCurrentEvent
import com.smart.album.events.ImageDisplayEvent
import com.smart.album.utils.PreferencesHelper
import org.greenrobot.eventbus.EventBus

class SettingTransitionEffectActivity : BaseActivity() {

    private var transitionEffect:Int = 0
    private lateinit var imgBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var lv_fade: LinearLayout
    private lateinit var lv_cross: LinearLayout
    private lateinit var lv_memory: LinearLayout


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_transition_effect)
        imgBack = findViewById(R.id.img_back)
        tvTitle = findViewById(R.id.tv_title)
        tvTitle.text = getString(R.string.setting)
        imgBack.setOnClickListener{
            finish()
        }

        transitionEffect =  PreferencesHelper.getInstance(this).getInt(PreferencesHelper.TRANSITION_EFFECT,0)

        lv_fade = findViewById(R.id.lv_fade)
        lv_cross = findViewById(R.id.lv_cross)
        lv_memory = findViewById(R.id.lv_memory)

        when (transitionEffect) {
            0 -> lv_fade.requestFocus()
            1 -> lv_cross.requestFocus()
            2 -> lv_memory.requestFocus()
            else -> lv_fade.requestFocus()

        }

        lv_fade.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TRANSITION_EFFECT,0)
            EventBus.getDefault().post(CloseCurrentEvent("close playing activity"))
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        }
        lv_cross.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TRANSITION_EFFECT,1)
            EventBus.getDefault().post(CloseCurrentEvent("close playing activity"))
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        }
        lv_memory.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TRANSITION_EFFECT,2)
            EventBus.getDefault().post(CloseCurrentEvent("close playing activity"))
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        }
    }

}
