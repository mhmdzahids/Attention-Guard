package com.attentionguard.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.os.Build

class AttentionAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                val scrollY = event.scrollY
                val scrollX = event.scrollX
                val timestamp = event.eventTime
                
                val deltaX = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) event.scrollDeltaX else 0
                val deltaY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) event.scrollDeltaY else 0
                
                trackScroll(scrollY, scrollX, timestamp, deltaY, deltaX)
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
        private var lastScrollY = -1
        private var lastScrollX = -1
        private var currentVelocity = 0f
        
        private var scrollCount = 0
        private var skipCount = 0

        fun getScrollVelocity(): Float {
            val elapsed = System.currentTimeMillis() - lastScrollTime
            if (elapsed > 10000) {
                currentVelocity = 0f
                scrollCount = 0
            }
            return if (currentVelocity > 0f) currentVelocity else 142f // 142f is baseline
        }

        fun getSkipRate(): Float {
            val elapsed = System.currentTimeMillis() - lastScrollTime
            if (elapsed > 10000) {
                skipCount = 0
                scrollCount = 0
            }
            return if (scrollCount > 0) (skipCount.toFloat() / scrollCount.toFloat()) * 100f else 64f
        }

        private fun trackScroll(y: Int, x: Int, time: Long, deltaY: Int, deltaX: Int) {
            val now = System.currentTimeMillis()
            val timeDeltaMs = now - lastScrollTime
            
            // Calculate distance scrolled in pixels
            val distance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && (deltaY != 0 || deltaX != 0)) {
                Math.sqrt((deltaY * deltaY + deltaX * deltaX).toDouble()).toFloat()
            } else {
                if (lastScrollY != -1 && lastScrollX != -1) {
                    val dy = y - lastScrollY
                    val dx = x - lastScrollX
                    Math.sqrt((dy * dy + dx * dx).toDouble()).toFloat()
                } else {
                    0f
                }
            }
            
            lastScrollY = y
            lastScrollX = x
            lastScrollTime = now
            
            if (timeDeltaMs in 1..3000) {
                val velocityPxPerSec = (distance / (timeDeltaMs / 1000f))
                // Apply a simple low-pass filter (exponential moving average) to smooth the velocity
                currentVelocity = if (currentVelocity == 0.0f) velocityPxPerSec else (0.7f * currentVelocity + 0.3f * velocityPxPerSec)
            } else {
                // If there's a long gap, reset velocity
                currentVelocity = 100f // default baseline for active scroll
            }
            
            scrollCount++
            // If the user scrolls a large distance (>200px) in a single event, increment skip count
            if (distance > 200f) {
                skipCount++
            }
        }
    }
}
