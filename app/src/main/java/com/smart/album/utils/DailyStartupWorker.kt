package com.smart.album.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.smart.album.R
import com.smart.album.WelcomeActivity

class DailyStartupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        Log.d("Startup==","doWork====")

        // 启动前台服务
//        val intent = Intent(applicationContext, StartupService::class.java)
//        applicationContext.startForegroundService(intent)

        val intent = Intent(applicationContext, WelcomeActivity::class.java)
        applicationContext.startActivity(intent)


//        // 创建启动Activity的Intent
//        val intent = Intent(applicationContext, WelcomeActivity::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        }
//
//        // 创建PendingIntent
//        val pendingIntent = PendingIntent.getActivity(
//            applicationContext,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // 创建通知
//        val notification = NotificationCompat.Builder(applicationContext, "your_channel_id")
//            .setContentTitle("Your App Title")
//            .setContentText("Click to open your app.")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .build()
//
//        // 显示通知
//        with(NotificationManagerCompat.from(applicationContext)) {
//            notify(1, notification)
//        }

        return Result.success()
    }
}