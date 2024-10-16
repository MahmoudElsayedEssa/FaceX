package com.example.facex.data.local.camera

import android.content.Context
import android.graphics.Bitmap
import android.os.BatteryManager
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


class FacesImageAnalyzer(
    val context: Context,
    private val onAnalyze: (Bitmap, Int) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastAnalysisTimestamp = 0L
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private var bitmapBuffer: Bitmap? = null
    private var frameSkipCounter = 0

    override fun analyze(imageProxy: ImageProxy) {
        val currentBitmap = getBitmapFromImageProxy(imageProxy)
//        val currentBitmap = imageProxy.toBitmap()
        val currentTime = System.currentTimeMillis()

        if (shouldAnalyze(currentTime, currentBitmap)) {
            onAnalyze(currentBitmap, imageProxy.imageInfo.rotationDegrees)

        }
        frameSkipCounter++
        imageProxy.close()
    }

    private fun shouldAnalyze(currentTime: Long, currentBitmap: Bitmap) =
        isBatteryAllow(currentTime) && isNotSharpImage(currentBitmap)

    private fun isNotSharpImage(bitmap: Bitmap): Boolean {
        val sharpness = OpenCVImageUtils.calculateSharpness(bitmap)
        return sharpness > OpenCVImageUtils.SHARPNESS_THRESHOLD
    }


    private fun getBitmapFromImageProxy(imageProxy: ImageProxy): Bitmap {
        return bitmapBuffer?.apply {
            copyImageToBuffer(imageProxy, this)
        } ?: createBitmapBuffer(imageProxy).also {
            bitmapBuffer = it
            copyImageToBuffer(imageProxy, it)
        }
    }

    private fun createBitmapBuffer(imageProxy: ImageProxy): Bitmap {
        return Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
    }

    private fun copyImageToBuffer(imageProxy: ImageProxy, bitmap: Bitmap) {
        bitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
    }

    private fun isBatteryAllow(currentTime: Long): Boolean {
        val timeSinceLastAnalysis = currentTime - lastAnalysisTimestamp
        val minInterval = when {
            batteryManager.isCharging -> 500 // 2 FPS when charging
            batteryManager.batteryLevel > 20 -> 1000 // 1 FPS when battery > 20%
            else -> 2000 // 0.5 FPS when battery <= 20%
        }
        return timeSinceLastAnalysis >= minInterval
    }

    private val BatteryManager.batteryLevel: Int
        get() = getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}
