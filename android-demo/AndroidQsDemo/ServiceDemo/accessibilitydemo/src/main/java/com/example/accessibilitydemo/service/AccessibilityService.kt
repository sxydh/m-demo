package com.example.accessibilitydemo.service

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicBoolean


class AccessibilityService : android.accessibilityservice.AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, "onAccessibilityEvent: ${EVENT_TYPES[event.eventType]}, ${event.packageName}")

        if (event.text.isNotEmpty()) {
            Log.w(TAG, "event.text: ${event.text}")
        }
    }

    override fun onInterrupt() {
    }

    companion object {
        private val TAG = AccessibilityService::class.java.name
        private val EVENT_TYPES = mutableMapOf<Int, String>()

        init {
            AccessibilityEvent::class.java.declaredFields
                .filter { Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) }
                .forEach {
                    try {
                        EVENT_TYPES[it.getInt(null)] = it.name
                    } catch (_: Exception) {
                    }
                }
        }
    }

}