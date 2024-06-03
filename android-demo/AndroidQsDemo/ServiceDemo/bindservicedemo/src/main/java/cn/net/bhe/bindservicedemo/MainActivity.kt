package cn.net.bhe.bindservicedemo

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.net.bhe.bindservicedemo.service.MyService
import cn.net.bhe.bindservicedemo.ui.theme.ServiceDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServiceDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }

        /* bindService 方式启动 Service */
        val intent = Intent(this, MyService::class.java)
        val serviceConnection = object : ServiceConnection {

            private val tag = ServiceConnection::class.java.simpleName

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val localBinder = service as MyService.LocalBinder
                val service = localBinder.getService()
                Log.d(tag, "onServiceConnected(), name = $name, service = $service")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(tag, "onServiceConnected(), name = $name")
            }

        }
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ServiceDemoTheme {
        Greeting("Android")
    }
}