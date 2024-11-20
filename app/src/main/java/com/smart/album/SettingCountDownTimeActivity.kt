package com.smart.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.smart.album.utils.PreferencesHelper
import org.apache.http.util.TextUtils

class SettingCountDownTimeActivity : BaseActivity() {

    private var timerMinutes:Int = 0
    private lateinit var imgBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var lv0: LinearLayout
    private lateinit var lv5: LinearLayout
    private lateinit var lv15: LinearLayout
    private lateinit var lv30: LinearLayout
    private lateinit var lv60: LinearLayout
    private lateinit var lv120: LinearLayout
    private lateinit var lvCustom: LinearLayout
    private lateinit var edTime: EditText


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_countdown_time)
        imgBack = findViewById(R.id.img_back)
        tvTitle = findViewById(R.id.tv_title)
        tvTitle.text = getString(R.string.setting)
        imgBack.setOnClickListener{
            finish()
        }

        timerMinutes =  PreferencesHelper.getInstance(this).getInt(PreferencesHelper.TIMER_MINUTES,0)

        lv0 = findViewById(R.id.lv_0)
        lv5 = findViewById(R.id.lv_5)
        lv15 = findViewById(R.id.lv_15)
        lv30 = findViewById(R.id.lv_30)
        lv60 = findViewById(R.id.lv_60)
        lv120 = findViewById(R.id.lv_120)
        lvCustom = findViewById(R.id.lv_custom)
        edTime = findViewById(R.id.ed_time)

        when (timerMinutes) {
            0 -> lv0.requestFocus()
            5 -> lv5.requestFocus()
            15 -> lv15.requestFocus()
            30 -> lv30.requestFocus()
            60 -> lv60.requestFocus()
            120 -> lv120.requestFocus()
            else -> {
                lvCustom.requestFocus()
                edTime.setText(timerMinutes.toString())
            }
        }

        lv0.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TIMER_MINUTES,0)
            App.instance.startCountdown()
            finish()
        }
        lv5.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TIMER_MINUTES,5)
            App.instance.startCountdown()
            finish()
        }
        lv15.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TIMER_MINUTES,15)
            App.instance.startCountdown()
            finish()
        }
        lv30.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TIMER_MINUTES,30)
            App.instance.startCountdown()
            finish()
        }
        lv60.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TIMER_MINUTES,60)
            App.instance.startCountdown()
            finish()
        }
        lv120.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TIMER_MINUTES,120)
            App.instance.startCountdown()
            finish()
        }
        lvCustom.setOnClickListener{
            if(!TextUtils.isEmpty(edTime.text.toString())
                && edTime.text.toString().toInt() > 10){
                val minute = edTime.text.toString().toInt()
                PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.TIMER_MINUTES,minute)
                App.instance.startCountdown()
                finish()
            } else {
                Toast.makeText(this,getString(R.string.time_toast),Toast.LENGTH_SHORT).show()
            }

        }
    }




}
