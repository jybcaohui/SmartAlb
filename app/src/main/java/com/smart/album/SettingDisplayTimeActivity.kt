package com.smart.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.smart.album.events.RefreshPageDataEvent
import com.smart.album.utils.PreferencesHelper
import org.apache.http.util.TextUtils
import org.greenrobot.eventbus.EventBus

class SettingDisplayTimeActivity : BaseActivity() {

    private var displaySeconds:Int = 0
    private lateinit var imgBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var lv5s: LinearLayout
    private lateinit var lv10s: LinearLayout
    private lateinit var lv15s: LinearLayout
    private lateinit var lv30s: LinearLayout
    private lateinit var lv1m: LinearLayout
    private lateinit var lv5m: LinearLayout
    private lateinit var lv10m: LinearLayout
    private lateinit var lvCustom: LinearLayout
    private lateinit var edTime: EditText


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_display_time)
        imgBack = findViewById(R.id.img_back)
        tvTitle = findViewById(R.id.tv_title)
        tvTitle.text = getString(R.string.setting)
        imgBack.setOnClickListener{
            finish()
        }

        displaySeconds =  PreferencesHelper.getInstance(this).getInt(PreferencesHelper.DISPLAY_TIME_SECONDS,10)

        lv5s = findViewById(R.id.lv_5s)
        lv10s = findViewById(R.id.lv_10s)
        lv15s = findViewById(R.id.lv_15s)
        lv30s = findViewById(R.id.lv_30s)
        lv1m = findViewById(R.id.lv_1m)
        lv5m = findViewById(R.id.lv_5m)
        lv10m = findViewById(R.id.lv_10m)
        lvCustom = findViewById(R.id.lv_custom)
        edTime = findViewById(R.id.ed_time)

        when (displaySeconds) {
            5 -> lv5s.requestFocus()
            10 -> lv10s.requestFocus()
            15 -> lv15s.requestFocus()
            30 -> lv30s.requestFocus()
            60 -> lv1m.requestFocus()
            300 -> lv5m.requestFocus()
            600 -> lv10m.requestFocus()
            else -> {
                lvCustom.requestFocus()
                if(displaySeconds>=60){
                    edTime.setText((displaySeconds/60).toString())
                }
            }
        }

        lv5s.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,5)
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            finish()
        }
        lv10s.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,10)
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            finish()
        }
        lv15s.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,15)
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            finish()
        }
        lv30s.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,30)
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            finish()
        }
        lv1m.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,60)
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            finish()
        }
        lv5m.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,300)
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            finish()
        }
        lv10m.setOnClickListener{
            PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,600)
            EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
            finish()
        }
        lvCustom.setOnClickListener{
            if(!TextUtils.isEmpty(edTime.text.toString())
                && edTime.text.toString().toInt() >= 1 ){
                val minute = edTime.text.toString().toInt()
                PreferencesHelper.getInstance(this).saveInt(PreferencesHelper.DISPLAY_TIME_SECONDS,minute*60)
                EventBus.getDefault().post(RefreshPageDataEvent("RefreshData"))
                finish()
            } else {
                Toast.makeText(this,getString(R.string.time_toast),Toast.LENGTH_SHORT).show()
            }

        }
    }




}
