package cn.net.bhe.layoutdemo

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import cn.net.bhe.layoutdemo.ui.theme.AndroidQsDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* 抽屉布局初始化 */
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        var drawerLeftButton: Button = findViewById(R.id.drawerLeftButton)
        drawerLeftButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        var drawerRightButton: Button = findViewById(R.id.drawerRightButton)
        drawerRightButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }
}