package cn.net.bhe.drawerlayoutdemo

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        var leftButton: Button = findViewById(R.id.leftButton)
        leftButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        var rightButton: Button = findViewById(R.id.rightButton)
        rightButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

}
