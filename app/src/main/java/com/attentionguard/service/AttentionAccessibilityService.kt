package com.attentionguard.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.os.Build

class AttentionAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val pkgName = event.packageName?.toString() ?: return
        if (pkgName.isNotEmpty()) {
            val oldPackage = activePackage
            val canonicalOld = oldPackage?.let { getCanonicalPackageName(it) }
            val canonicalNew = getCanonicalPackageName(pkgName)
            if (canonicalOld != canonicalNew) {
                activePackage = pkgName
                activePackageStartTime = System.currentTimeMillis()
                
                // Reset scroll tracking values on app/package change
                lastScrollTime = 0L
                lastScrollY = -1
                lastScrollX = -1
                currentVelocity = 0f
            }
        }

        val isTargetApp = pkgName == "com.google.android.youtube" ||
                          pkgName == "com.instagram.android" ||
                          pkgName == "com.zhiliaoapp.musically" ||
                          pkgName == "com.ss.android.ugc.trill" ||
                          pkgName == "com.ss.android.ugc.aweme" ||
                          pkgName == "com.ss.android.ugc.aweme.lite"
                           
        if (!isTargetApp) return
        
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
        
        var activePackage: String? = null
        var activePackageStartTime = 0L

        fun getCanonicalPackageName(pkg: String): String {
            return when (pkg) {
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
                "com.ss.android.ugc.aweme",
                "com.ss.android.ugc.aweme.lite" -> "com.zhiliaoapp.musically"
                else -> pkg
            }
        }
        
        private var lastScrollTime = 0L
        private var lastScrollY = -1
        private var lastScrollX = -1
        private var currentVelocity = 0f
        private var averageVelocity = 142f // default baseline
        private var velocityMeasurementsCount = 0
        
        private var scrollCount = 0
        private var skipCount = 0

        fun getScrollVelocity(): Float {
            return if (averageVelocity.isNaN() || averageVelocity.isInfinite()) 142f else averageVelocity
        }

        fun getSkipRate(): Float {
            val rate = if (scrollCount > 0) (skipCount.toFloat() / scrollCount.toFloat()) * 100f else 0f
            return if (rate.isNaN() || rate.isInfinite()) 0f else rate
        }

        private fun trackScroll(y: Int, x: Int, time: Long, deltaY: Int, deltaX: Int) {
            val now = System.currentTimeMillis()
            val timeDeltaMs = now - lastScrollTime
            
            // Calculate distance scrolled in pixels with Long to avoid integer overflow
            val distance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && (deltaY != 0 || deltaX != 0)) {
                val sum = (deltaY.toLong() * deltaY.toLong() + deltaX.toLong() * deltaX.toLong())
                Math.sqrt(Math.max(0.0, sum.toDouble())).toFloat()
            } else {
                if (lastScrollY != -1 && lastScrollX != -1) {
                    val dy = (y - lastScrollY).toLong()
                    val dx = (x - lastScrollX).toLong()
                    val sum = (dy * dy + dx * dx)
                    Math.sqrt(Math.max(0.0, sum.toDouble())).toFloat()
                } else {
                    0f
                }
            }
            
            val safeDistance = if (distance.isNaN() || distance.isInfinite()) 0f else distance
            
            lastScrollY = y
            lastScrollX = x
            lastScrollTime = now
            
            if (timeDeltaMs in 1..3000) {
                // Stabilize calculations to prevent high spikes when events occur in quick succession (< 80ms)
                val timeSeconds = Math.max(80f, timeDeltaMs.toFloat()) / 1000f
                val velocityPxPerSec = (safeDistance / timeSeconds).coerceIn(0f, 1000f)
                
                // Apply a simple low-pass filter (exponential moving average) to smooth the velocity
                currentVelocity = if (currentVelocity == 0.0f || currentVelocity.isNaN()) velocityPxPerSec else (0.7f * currentVelocity + 0.3f * velocityPxPerSec)
                if (currentVelocity.isNaN() || currentVelocity.isInfinite()) {
                    currentVelocity = 100f
                }
                
                // Track cumulative average velocity for stability
                velocityMeasurementsCount++
                averageVelocity = if (velocityMeasurementsCount == 1) {
                    currentVelocity
                } else {
                    val newAverage = (averageVelocity * 0.95f) + (currentVelocity * 0.05f)
                    if (newAverage.isNaN() || newAverage.isInfinite()) 142f else newAverage
                }

                // Increment counts inside the delta window to ensure accurate calculations
                scrollCount++
                // A "skip" is defined as a rapid, frantic fling gesture (speed > 450 px/s)
                if (velocityPxPerSec > 450f) {
                    skipCount++
                }
            } else {
                // Reset current velocity on long gap
                currentVelocity = 100f
            }
        }
    }
}
