package cn.net.bhe.servicedemo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class FirstService : Service() {

    private val tag = FirstService::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val ret = super.onStartCommand(intent, flags, startId)
        Log.i(tag, "onStartCommand()")
        return ret
    }

    override fun onBind(intent: Intent): IBinder? {
        // 不会触发
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        // 不会触发
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy()")
    }

}