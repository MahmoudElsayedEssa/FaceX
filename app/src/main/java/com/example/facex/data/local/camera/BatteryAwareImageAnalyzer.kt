package com.example.facex.data.local.camera

import android.content.Context
import android.os.BatteryManager
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class BatteryAwareImageAnalyzer(
    context: Context,
    private val delegate: FacesImageAnalyzer
) : ImageAnalysis.Analyzer {

    private var lastAnalysisTimestamp = 0L
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (shouldAnalyze(currentTime)) {
            delegate.analyze(imageProxy)
            lastAnalysisTimestamp = currentTime
        } else {
            imageProxy.close()
        }
    }

    private fun shouldAnalyze(currentTime: Long): Boolean {
        val timeSinceLastAnalysis = currentTime - lastAnalysisTimestamp
        val minInterval = when {
            batteryManager.isCharging -> 500 // 2 FPS when charging
            batteryManager.batteryLevel > 20 -> 1000 // 1 FPS when battery > 20%
            else -> 2000 // 0.5 FPS when battery <= 20%
        }
        return timeSinceLastAnalysis >= minInterval
    }

    private val BatteryManager.isCharging: Boolean
        get() = getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING

    private val BatteryManager.batteryLevel: Int
        get() = getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}
