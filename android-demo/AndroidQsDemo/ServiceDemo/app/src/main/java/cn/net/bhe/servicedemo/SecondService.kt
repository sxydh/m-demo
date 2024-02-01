package cn.net.bhe.servicedemo

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class SecondService : Service() {

    private val tag = SecondService::class.java.simpleName
    private var binder: LocalBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): SecondService {
            return this@SecondService
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 不会触发
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(tag, "onBind()")
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        val ret = super.onUnbind(intent)
        Log.i(tag, "onUnbind()")
        return ret
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy()")
    }

}