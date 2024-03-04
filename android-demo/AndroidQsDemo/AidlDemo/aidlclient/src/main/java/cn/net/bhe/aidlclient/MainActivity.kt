package cn.net.bhe.aidlclient

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import cn.net.bhe.aidlserver.IRemoteService

class MainActivity : ComponentActivity() {

    private val tag = MainActivity::class.java.simpleName

    private var iRemoteService: IRemoteService? = null
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(tag, "onServiceConnected")
            iRemoteService = IRemoteService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            iRemoteService = null
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent()
        intent.action = "cn.net.bhe.aidlserver.IRemoteService"
        intent.setPackage("cn.net.bhe.aidlserver")
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        findViewById<Button>(R.id.button).setOnClickListener {
            Log.i(tag, "$iRemoteService")
            iRemoteService?.basicTypes(1, 1L, true, 1.0F, 1.0, "Hello World")
        }
    }
}