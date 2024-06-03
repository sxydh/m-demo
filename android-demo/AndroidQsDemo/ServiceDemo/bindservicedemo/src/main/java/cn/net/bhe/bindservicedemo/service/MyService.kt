package cn.net.bhe.bindservicedemo.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.UUID
import java.util.concurrent.Executors

class MyService : Service() {

    private val tag = MyService::class.java.simpleName
    private val binder: LocalBinder = LocalBinder()
    private val threadPool = Executors.newFixedThreadPool(1)

    inner class LocalBinder : Binder() {
        fun getService(): MyService {
            return this@MyService
        }
    }

    override fun onCreate() {
        Log.i(tag, "onCreate()")
        super.onCreate()

        execTask()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(tag, "onStartCommand()")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(tag, "onBind()")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(tag, "onUnbind()")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.i(tag, "onDestroy()")
        super.onDestroy()
    }

    private fun execTask() {
        threadPool.submit {
            for (i in 0..3) {
                Log.d(tag, UUID.randomUUID().toString())
                Thread.sleep(2000)
            }
        }
    }

}