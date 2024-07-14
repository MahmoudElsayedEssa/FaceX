package com.example.facex.data.local.camera

import android.app.Activity
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


class ImageAnalyzer(private val onAnalyze: (bitmap: Bitmap, rotationDegrees: Int) -> Unit) :
    ImageAnalysis.Analyzer {

    private lateinit var bitmapBuffer: Bitmap
    private lateinit var previousBitmap: Bitmap
    private var isPreviousBitmapInitialized = false

    override fun analyze(imageProxy: ImageProxy) {
        initializeBitmapBuffer(imageProxy)
        copyImageToBuffer(imageProxy)


        logInferenceTime {
            onAnalyze(bitmapBuffer, imageProxy.imageInfo.rotationDegrees)
        }

//        bitmapBuffer.takeIf { isNotSharpImage(it) }
//            ?.also {
//                logInferenceTime {
//                    onAnalyze(bitmapBuffer, imageProxy.imageInfo.rotationDegrees)
//                }
//            }

        updatePreviousBitmap()
    }

    private fun initializeBitmapBuffer(imageProxy: ImageProxy) {
        if (!::bitmapBuffer.isInitialized) {
            bitmapBuffer =
                Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
        }
    }

    private fun copyImageToBuffer(imageProxy: ImageProxy) {
        imageProxy.use {
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
        }
    }

    private fun isNotSharpImage(bitmap: Bitmap): Boolean {
        val sharpness = OpenCVImageUtils.calculateSharpness(bitmap)
        return sharpness > OpenCVImageUtils.SHARPNESS_THRESHOLD
    }

    private fun isSignificantChange(currentBitmap: Bitmap, previousBitmap: Bitmap): Boolean {
        return OpenCVImageUtils.isSignificantChange(currentBitmap, previousBitmap)
    }

    private fun logInferenceTime(block: () -> Unit) {
        val (inferenceTime, timeType) = measureInferenceTime(block)
        Log.d(TAG, "inferenceTime: $inferenceTime $timeType")
    }

    private inline fun measureInferenceTime(block: () -> Unit): Pair<Long, String> {
        val startTime = System.nanoTime()
        block()
        val endTime = System.nanoTime()
        val elapsedTime = endTime - startTime

        return when {
            elapsedTime < 1_000 -> Pair(elapsedTime, "nanoseconds")
            elapsedTime < 1_000_000 -> Pair(elapsedTime / 1_000, "microseconds")
            else -> Pair(elapsedTime / 1_000_000, "milliseconds")
        }
    }

    private fun updatePreviousBitmap() {
        if (!::previousBitmap.isInitialized) {
            previousBitmap = Bitmap.createBitmap(
                bitmapBuffer.width, bitmapBuffer.height, Bitmap.Config.ARGB_8888
            )
        }
        previousBitmap = bitmapBuffer.copy(Bitmap.Config.ARGB_8888, false)
        isPreviousBitmapInitialized = true
    }


    companion object {
        private const val TAG = "ImageAnalyzer"
    }
}
