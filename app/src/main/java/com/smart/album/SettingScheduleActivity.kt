package com.smart.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.smart.album.utils.PreferencesHelper


class SettingScheduleActivity : BaseActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var imgBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvSave: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvStopTime: TextView
    private lateinit var switchStart: Switch
    private lateinit var switchStop: Switch
    private lateinit var startTimePicker: TimePicker
    private lateinit var stopTimePicker: TimePicker
    private var startTime:String = ""
    private var startOn:Boolean = false
    private var stopTime:String = ""
    private var stopOn:Boolean = false

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_schedule)
        rootLayout = findViewById(R.id.root)
        imgBack = findViewById(R.id.img_back)
        tvTitle = findViewById(R.id.tv_title)
        tvSave = findViewById(R.id.tv_save)
        tvStartTime = findViewById(R.id.tv_start_time)
        tvStopTime = findViewById(R.id.tv_stop_time)
        startTimePicker = findViewById(R.id.startTimePicker)
        stopTimePicker = findViewById(R.id.stopTimePicker)
        switchStart = findViewById(R.id.switch_start)
        switchStop = findViewById(R.id.switch_stop)
        stopTimePicker = findViewById(R.id.stopTimePicker)
        tvTitle.text = getString(R.string.schedule_setting)

        imgBack.setOnClickListener{
            finish()
        }

        switchStart.requestFocus()
        // 设置开关状态改变监听器
        switchStart.setOnCheckedChangeListener { _, isChecked ->
            startOn = isChecked
        }
        // 设置开关状态改变监听器
        switchStop.setOnCheckedChangeListener { _, isChecked ->
            stopOn = isChecked
        }

        // 监听时间改变
        startTimePicker.setOnTimeChangedListener { _, hourOfDay, minute -> // 在这里处理时间变化
        }

        stopTimePicker.setOnTimeChangedListener { _, hourOfDay, minute -> // 在这里处理时间变化
        }

        //默认开关状态
        startOn = PreferencesHelper.getInstance(this@SettingScheduleActivity).getBoolean(PreferencesHelper.SCHEDULE_START_ON,false)
        stopOn = PreferencesHelper.getInstance(this@SettingScheduleActivity).getBoolean(PreferencesHelper.SCHEDULE_STOP_ON,false)
        switchStart.setChecked(startOn)
        switchStop.setChecked(stopOn)
        //TimePicker默认时间
        startTime = PreferencesHelper.getInstance(this@SettingScheduleActivity).getStr(PreferencesHelper.SCHEDULE_START_TIME).toString()
        stopTime = PreferencesHelper.getInstance(this@SettingScheduleActivity).getStr(PreferencesHelper.SCHEDULE_STOP_TIME).toString()
        Log.d("Startup==", "init====startTime===$startTime")
        Log.d("Startup==", "init====stopTime===$stopTime")
        if(!TextUtils.isEmpty(startTime) && startTime.split(":").size == 2){
            startTimePicker.hour = startTime.split(":")[0].toInt()
            startTimePicker.minute = startTime.split(":")[1].toInt()
            tvStartTime.text = startTime
        }
        if(!TextUtils.isEmpty(stopTime) && stopTime.split(":").size == 2){
            stopTimePicker.hour = stopTime.split(":")[0].toInt()
            stopTimePicker.minute = stopTime.split(":")[1].toInt()
            tvStopTime.text = stopTime
        }
        tvSave.setOnClickListener{
            startTime = String.format("%02d", startTimePicker.hour)+":"+String.format("%02d", startTimePicker.minute)
            PreferencesHelper.getInstance(this@SettingScheduleActivity).saveStr(PreferencesHelper.SCHEDULE_START_TIME,startTime)
            PreferencesHelper.getInstance(this@SettingScheduleActivity).saveBoolean(PreferencesHelper.SCHEDULE_START_ON,startOn)
            tvStartTime.text = startTime

            stopTime = String.format("%02d", stopTimePicker.hour)+":"+String.format("%02d", stopTimePicker.minute)
            PreferencesHelper.getInstance(this@SettingScheduleActivity).saveStr(PreferencesHelper.SCHEDULE_STOP_TIME,stopTime)
            PreferencesHelper.getInstance(this@SettingScheduleActivity).saveBoolean(PreferencesHelper.SCHEDULE_STOP_ON,stopOn)
            tvStopTime.text = stopTime


            Log.d("Startup==","startTime====$startTime")
            Log.d("Startup==","startOn===$startOn")
            Log.d("Startup==","stopTime====$stopTime")
            Log.d("Startup==","stopOn===$stopOn")

            scheduleStartDailyWork()
            if(stopOn && !TextUtils.isEmpty(stopTime)){
                App.instance.startAutoStopCountdown()
            }

            Toast.makeText(this, "Set up for success", Toast.LENGTH_SHORT).show()
            Log.d("Startup==","scheduleDailyWork====")
            finish()
        }

    }

}
