package com.smart.album

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.smart.album.receiver.TimerReceiver
import com.smart.album.utils.DailyStartupWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

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

            // 创建一个周期性的工作请求
            val dailyWorkRequest = PeriodicWorkRequest.Builder(
                DailyStartupWorker::class.java,
                15, // 每24小时
                TimeUnit.MINUTES
            ).build()

            // 将工作请求加入WorkManager
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_startup_work",
                ExistingPeriodicWorkPolicy.REPLACE, // 如果已存在相同名称的任务，则替换
                dailyWorkRequest
            )

//            if (!isExactAlarmsAllowed()) {
//                // 引导用户到设置界面开启权限
//                showPermissionDialog()
//            } else {
//                // 设置精确闹钟
//                setDailyAlarm()
//            }
        }

    }

    private fun setDailyAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent("com.example.myapp.START_APP_DAILY")
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            })

        var hour = edHour.text.toString().toInt()
        var minute = edMinute.text.toString().toInt()
        // 设置定时任务的时间
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, hour) // 例如，每天早上8点
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            // 如果设置的时间已经过去，那么就将时间改为第二天的同一时间
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }


        // 设置每天重复
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Toast.makeText(this, "定时任务已设置", Toast.LENGTH_SHORT).show()
    }

    private fun isExactAlarmsAllowed(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // 在API级别低于31的设备上，默认允许精确闹钟
        }
    }

    companion object {
        const val REQUEST_CODE_SCHEDULE_EXACT_ALARM = 1
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("精确闹钟权限")
            .setMessage("为了能够每天定时启动应用，请允许设置精确闹钟。")
            .setPositiveButton("前往设置") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
                startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM)
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCHEDULE_EXACT_ALARM) {
            if (isExactAlarmsAllowed()) {
                // 用户授予了权限，现在可以设置精确闹钟
                setDailyAlarm()
            } else {
                // 用户拒绝了权限，处理这种情况
                showPermissionDialog()
            }
        }
    }

}
