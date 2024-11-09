package com.smart.album.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.smart.album.WelcomeActivity

class DailyStartupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("doWork====","doWork====")
        // 启动你的主活动
        val intent = Intent(applicationContext, WelcomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        applicationContext.startActivity(intent)
        return Result.success()
    }
}