package com.attentionguard.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AttentionAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                val scrollY = event.scrollY
                val scrollX = event.scrollX
                val timestamp = event.eventTime
                trackScroll(scrollY, scrollX, timestamp)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service Connected")
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    companion object {
        private const val TAG = "AttentionAccessibility"
        private var instance: AttentionAccessibilityService? = null
        
        private var lastScrollTime = 0L
        private var scrollCount = 0
        private var skipCount = 0

        fun getScrollVelocity(): Float {
            val elapsed = System.currentTimeMillis() - lastScrollTime
            if (elapsed > 10000) {
                scrollCount = 0
            }
            return if (scrollCount > 0) scrollCount * 15f else 142f
        }

        fun getSkipRate(): Float {
            return if (scrollCount > 0) (skipCount.toFloat() / scrollCount.toFloat()) * 100f else 64f
        }

        private fun trackScroll(y: Int, x: Int, time: Long) {
            lastScrollTime = System.currentTimeMillis()
            scrollCount++
            if (Math.abs(y) > 200) {
                skipCount++
            }
        }
    }
}
