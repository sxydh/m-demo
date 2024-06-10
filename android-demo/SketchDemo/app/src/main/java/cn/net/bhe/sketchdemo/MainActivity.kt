package cn.net.bhe.sketchdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import cn.net.bhe.sketchdemo.view.CanvasView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(CanvasView(this))
    }
}