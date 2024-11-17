package com.smart.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.smart.album.utils.DailyStartupWorker
import com.smart.album.utils.PreferencesHelper
import com.smart.album.views.LoadingDialog
import java.util.Calendar
import java.util.concurrent.TimeUnit


open class BaseActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog()

        // 添加到活动列表
        App.instance.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 从活动列表中移除
        App.instance.removeActivity(this)
    }

    fun showLoading(){
        loadingDialog?.show(supportFragmentManager, "loading_dialog")
    }
    fun hideLoading(){
        loadingDialog?.dismiss()
    }

    fun scheduleStartDailyWork() {
        val initialDelay = calculateInitialDelay()
        if(initialDelay <= 0){
            return
        }
        // 创建一个一次性的工作请求
        val workRequest = OneTimeWorkRequestBuilder<DailyStartupWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_work_tag") // 可选：添加标签以便后续管理
            .build()

        // 使用WorkManager来调度任务
        WorkManager.getInstance(this).enqueueUniqueWork(
            "daily_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    // 计算初始延迟,定时开启关闭或未设置时间时，返回0，不开启自启动
    private fun calculateInitialDelay(): Long {
        val startTime = PreferencesHelper.getInstance(this).getStr(
            PreferencesHelper.SCHEDULE_START_TIME).toString()
        val startOn = PreferencesHelper.getInstance(this).getBoolean(PreferencesHelper.SCHEDULE_START_ON,false)
        if(startOn && !TextUtils.isEmpty(startTime) && startTime.split(":").size == 2){
            val hour = startTime.split(":")[0].toInt()
            val minute = startTime.split(":")[1].toInt()
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
//        return 1000 * 60 * 2//2分钟,测试
    }


}

