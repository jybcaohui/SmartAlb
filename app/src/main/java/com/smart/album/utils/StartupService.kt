package com.smart.album.utils
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smart.album.R
import com.smart.album.WelcomeActivity

class StartupService : Service() {

    private val CHANNEL_ID = "StartupServiceChannel"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 创建通知渠道（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Startup Service Channel"
            val descriptionText = "Channel for startup service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // 注册通知渠道
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 创建通知
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Startup Service")
            .setContentText("Starting the app...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        Log.d("Startup==","startForeground====")
        // 启动服务为前台服务
        startForeground(1, notification)

        // 启动你的主Activity
        val activityIntent = Intent(this, WelcomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(activityIntent)

        // 服务执行完毕后停止
        stopSelf()

        return START_NOT_STICKY
    }
}