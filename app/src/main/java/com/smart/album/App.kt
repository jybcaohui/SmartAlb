package com.smart.album

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.smart.album.utils.PreferencesHelper
import kotlin.system.exitProcess

class App : Application() {
    private val countdownHandler = Handler(Looper.getMainLooper())
    companion object {
        lateinit var instance: App
        var timerLast = 0L//定时关闭，剩余时间(毫秒)
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        startCountdown()

        createNotificationChannel(this)
    }


     fun startCountdown() {
         val timerMinutes =  PreferencesHelper.getInstance(this).getInt(
             PreferencesHelper.TIMER_MINUTES,0)
         countdownHandler.removeCallbacksAndMessages(null)
         if(timerMinutes > 0){
             val totalMillis = (timerMinutes * 60 * 1000).toLong()
             timerLast = totalMillis
             val countDownTimer = object : CountDownTimer(totalMillis, 1000) {
                 override fun onTick(millisUntilFinished: Long) {
                     // 已进行的timer（毫秒）
                     timerLast = millisUntilFinished
                 }
                 override fun onFinish() {
                 }
             }
             countDownTimer.start()

             val runnable = Runnable {
                 // 尝试关闭应用程序
                 closeApp()
             }
             // 倒计时timerMinutes分钟
             countdownHandler.postDelayed(runnable, (timerMinutes*60*1000).toLong())
         }

    }

    private fun closeApp() {
        // 这里可以添加更多的清理工作
        // 结束所有活动
        for (activity in activityList) {
            activity.finish()
        }
        // 退出JVM
        exitProcess(0)
    }

    // 用来保存当前打开的所有Activity
    private val activityList = mutableListOf<android.app.Activity>()

    // 添加Activity到列表
    fun addActivity(activity: android.app.Activity) {
        activityList.add(activity)
    }

    // 从列表中移除Activity
    fun removeActivity(activity: android.app.Activity) {
        activityList.remove(activity)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Your Channel Name"
            val descriptionText = "Your Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("your_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}