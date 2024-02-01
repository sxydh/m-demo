package cn.net.bhe.servicedemo

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* startService 方式启动 Service */
        findViewById<Button>(R.id.runFirstService).setOnClickListener {
            val intent = Intent(this, FirstService::class.java)
            startService(intent)
        }

        /* bindService 方式启动 Service */
        val serviceConnection = object : ServiceConnection {

            private val tag = ServiceConnection::class.java.simpleName

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val localBinder = service as SecondService.LocalBinder
                val service = localBinder.getService()
                Log.i(tag, "onServiceConnected(), service = $service")
            }

            override fun onServiceDisconnected(name: ComponentName) {

            }
        }
        findViewById<Button>(R.id.runSecondService).setOnClickListener {
            val intent = Intent(this, SecondService::class.java)
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
            unbindService(serviceConnection)
        }
    }

}
