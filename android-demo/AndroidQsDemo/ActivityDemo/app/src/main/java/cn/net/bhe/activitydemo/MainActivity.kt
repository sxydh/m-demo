package cn.net.bhe.activitydemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import cn.net.bhe.activitydemo.helper.InstManager

class MainActivity : ComponentActivity() {

    private val tag = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tag, "onCreate()")
        setContentView(R.layout.activity_main)

        /* 显示启动 SecondActivity */
        findViewById<Button>(R.id.to2Button).setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

        /* 隐式启动 ThirdActivity */
        // 通过规则匹配目标 Activity
        findViewById<Button>(R.id.to3Button).setOnClickListener {
            val intent = Intent()
            intent.action = "anyId.ThirdActivity"
            startActivity(intent)
        }
        // 通过协议跳转目标 Activity
        findViewById<Button>(R.id.to4Button).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("any_schema://any_host/any_path_fourth_activity"))
            startActivity(intent)
        }

        /* 显示停止 */
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            for (activity in InstManager.getActivityList()) {
                activity.finish()
            }
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(tag, "onStart()")
    }

    override fun onPause() {
        super.onPause()
        Log.i(tag, "onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume()")
    }

    override fun onStop() {
        super.onStop()
        Log.i(tag, "onStop()")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(tag, "onRestart()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "onDestroy()")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.i(tag, "onSaveInstanceState()")
    }

}
