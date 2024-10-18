package com.example.accessibilitydemo.service

import android.util.Log
import android.view.accessibility.AccessibilityEvent


class AccessibilityService : android.accessibilityservice.AccessibilityService() {

    private val tag = AccessibilityService::class.java.name

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(tag, "onServiceConnected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(tag, "onAccessibilityEvent: ${event.eventType}, ${event.packageName}")
    }

    override fun onInterrupt() {
    }

}