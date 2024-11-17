package com.smart.album

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.smart.album.utils.PreferencesHelper
import java.util.Calendar
import kotlin.system.exitProcess

class App : Application() {
    private val countdownHandler = Handler(Looper.getMainLooper())
    private val countdownAutoStopHandler = Handler(Looper.getMainLooper())
    companion object {
        lateinit var instance: App
        var timerLast = 0L//定时关闭，剩余时间(毫秒)
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        startCountdown()
        startAutoStopCountdown()

        createNotificationChannel(this)
    }


    /**
     * Timer 运行时长定时关闭
     */
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

    /**
     * 如果自动关闭按钮开启
     * 且Timer运行时长定时未开启，或者Timer运行时长定时大于自动关闭定时时长，则开启自动关闭倒计时
     * 该方法会先执行关闭应用操作
     * Timer运行时长定时未开启的情况下，startCountdown无论如何都运行，保证TimerSetting中有数据展示
     */
    fun startAutoStopCountdown() {
        //定时
        val timerMillis=  PreferencesHelper.getInstance(this).getInt(
            PreferencesHelper.TIMER_MINUTES,0) * 60 * 1000
        val autoStopDelay  = calculateAutoStopDelay()
        countdownAutoStopHandler.removeCallbacksAndMessages(null)
        if(autoStopDelay > 0 && (timerMillis == 0 || timerMillis > autoStopDelay)){
            val runnable = Runnable {
                // 尝试关闭应用程序
                closeApp()
            }
            // 倒计时毫秒
            countdownAutoStopHandler.postDelayed(runnable, autoStopDelay)
        }
    }


    // 计算延迟关闭时间(毫秒),定时关闭按钮关闭或未设置时间时，返回0，不开启自动关闭
    private fun calculateAutoStopDelay(): Long {
        val stopTime = PreferencesHelper.getInstance(this).getStr(
            PreferencesHelper.SCHEDULE_STOP_TIME).toString()
        val stopOn = PreferencesHelper.getInstance(this).getBoolean(PreferencesHelper.SCHEDULE_STOP_ON,false)
        if(stopOn && !TextUtils.isEmpty(stopTime) && stopTime.split(":").size == 2){
            val hour = stopTime.split(":")[0].toInt()
            val minute = stopTime.split(":")[1].toInt()
            val now = Calendar.getInstance()
            val nextRun = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (now.after(this)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            return nextRun.timeInMillis - now.timeInMillis
        }
        return 0
    }
}