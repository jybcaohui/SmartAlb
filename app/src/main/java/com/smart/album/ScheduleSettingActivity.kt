package com.smart.album

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smart.album.receiver.TimerReceiver
import java.util.Calendar

class ScheduleSettingActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var imgBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvSave: TextView
    private lateinit var edHour: EditText
    private lateinit var edMinute: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_schedule)
        rootLayout = findViewById(R.id.root)
        imgBack = findViewById(R.id.img_back)
        tvTitle = findViewById(R.id.tv_title)
        edHour = findViewById(R.id.ed_hour)
        edMinute = findViewById(R.id.ed_minute)
        tvSave = findViewById(R.id.tv_save)
        tvTitle.text = getString(R.string.schedule_setting)

        imgBack.setOnClickListener{
            finish()
        }
        tvSave.setOnClickListener{
            setDailyAlarm()
        }

    }

    private fun setDailyAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TimerReceiver::class.java).apply {
            action = "com.example.ACTION_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        var hour = edHour.text.toString().toInt()
        var minute = edMinute.text.toString().toInt()
        // 设置定时任务的时间
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour) // 设置小时，例如10点
            set(Calendar.MINUTE, minute)      // 设置分钟，例如30分
            set(Calendar.SECOND, 0)       // 设置秒
        }

        // 如果设置的时间已经过去，则将时间设置为明天同一时间
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 设置重复定时任务
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Toast.makeText(this, "定时任务已设置", Toast.LENGTH_SHORT).show()
    }

}
