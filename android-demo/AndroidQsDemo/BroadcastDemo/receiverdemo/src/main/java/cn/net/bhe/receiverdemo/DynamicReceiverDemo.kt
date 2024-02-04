package cn.net.bhe.receiverdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DynamicReceiverDemo : BroadcastReceiver() {

    private val tag = DynamicReceiverDemo::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "intent.extra.ts = ${intent.getStringExtra("ts")}")
    }

}