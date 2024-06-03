package cn.net.bhe.foregrounddemo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.UUID
import java.util.concurrent.Executors


class MyService : Service() {

    private val tag = MyService::class.java.simpleName
    private val threadPool = Executors.newFixedThreadPool(1)
    private val channelId = UUID.randomUUID().toString()
    private val channelName = "Channel: $channelId"
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH))
        execTask()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, channelId)
            .build()
        startForeground(UUID.randomUUID().hashCode(), notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun execTask() {
        threadPool.submit {
            while (true) {
                Log.d(tag, UUID.randomUUID().toString())
                Thread.sleep(2000)
            }
        }
    }

}