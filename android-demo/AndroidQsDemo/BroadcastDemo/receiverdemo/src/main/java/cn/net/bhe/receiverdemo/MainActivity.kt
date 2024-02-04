package cn.net.bhe.receiverdemo

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 静态注册 BroadcastReceiver */
        // 参见 AndroidManifest.xml
        // 静态注册的 BroadcastReceiver 需要广播时指定 ComponentName

        /* 动态注册 BroadcastReceiver */
        val dynamicReceiverDemo = DynamicReceiverDemo()
        val intentFilter = IntentFilter("actionName")
        registerReceiver(dynamicReceiverDemo, intentFilter, RECEIVER_EXPORTED)
    }
}
