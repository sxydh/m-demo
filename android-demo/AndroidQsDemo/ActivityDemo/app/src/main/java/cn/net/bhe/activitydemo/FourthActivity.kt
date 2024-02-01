package cn.net.bhe.activitydemo

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import cn.net.bhe.activitydemo.helper.InstManager

class FourthActivity : ComponentActivity() {

    private val tag = FourthActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tag, "onCreate()")
        setContentView(R.layout.activity_fourth)
        InstManager.addActivity(this)
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
