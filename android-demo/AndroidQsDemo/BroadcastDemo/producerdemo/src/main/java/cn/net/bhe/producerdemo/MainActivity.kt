package cn.net.bhe.producerdemo

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import cn.net.bhe.producerdemo.helper.InstManager

class MainActivity : ComponentActivity() {

    private val tag = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* 显示广播 */
        findViewById<Button>(R.id.explicitButton).setOnClickListener {
            it as Button
            it.isEnabled = false
            InstManager.EXECUTORS.submit {
                try {
                    while (true) {
                        val intent = Intent("actionName")
                        intent.component = ComponentName("cn.net.bhe.receiverdemo", "cn.net.bhe.receiverdemo.StaticReceiverDemo")
                        intent.putExtra("ts", "${System.currentTimeMillis()}")
                        sendBroadcast(intent)
                        Log.d(tag, "explicitButton, sendBroadcast = ${intent.extras}")
                        Thread.sleep(3000)
                    }
                } catch (e: Exception) {
                    Log.e(tag, e.localizedMessage)
                }
            }
        }

        /* 隐式广播 */
        findViewById<Button>(R.id.implicitButton).setOnClickListener {
            it as Button
            it.isEnabled = false
            InstManager.EXECUTORS.submit {
                try {
                    while (true) {
                        val intent = Intent("actionName")
                        intent.putExtra("ts", "${System.currentTimeMillis()}")
                        sendBroadcast(intent)
                        Log.d(tag, "implicitButton, sendBroadcast = ${intent.extras}")
                        Thread.sleep(3000)
                    }
                } catch (e: Exception) {
                    Log.e(tag, e.localizedMessage)
                }
            }
        }
    }

}
