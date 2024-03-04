package cn.net.bhe.aidlserver

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ServerService : Service() {

    private val tag = ServerService::class.java.simpleName
    private val binder: IRemoteService.Stub = object : IRemoteService.Stub() {

        override fun basicTypes(anInt: Int, aLong: Long, aBoolean: Boolean, aFloat: Float, aDouble: Double, aString: String) {
            Log.i(tag,"$anInt: Int, $aLong: Long, $aBoolean: Boolean, $aFloat: Float, $aDouble: Double, $aString: String")
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

}