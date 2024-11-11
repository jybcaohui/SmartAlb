package com.smart.album.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.smart.album.WelcomeActivity

class DailyStartupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        Log.d("Startup==","doWork====")

        // 启动前台服务
        val intent = Intent(applicationContext, StartupService::class.java)
        applicationContext.startForegroundService(intent)


//        // 启动你的主活动
//        val intent = Intent(applicationContext, WelcomeActivity::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//        try {
//            applicationContext.startActivity(intent)
//            Log.d("doWork====", "WelcomeActivity started successfully.")
//        } catch (e: Exception) {
//            Log.e("doWork====", "Failed to start WelcomeActivity: ${e.message}")
//        }
        return Result.success()
    }
}