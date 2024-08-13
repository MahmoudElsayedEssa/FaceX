package com.example.facex.ui.screens.camera_face_recognition.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.BatteryManager
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class FacesImageAnalyzer(
    private val lifecycleScope: CoroutineScope,
    private val onAnalyze: suspend (Bitmap, Int) -> Unit,
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        lifecycleScope.launch(Dispatchers.Default) {
            val currentBitmap = imageProxy.toBitmap()
            try {
                onAnalyze(currentBitmap, rotationDegrees)
            } finally {
                withContext(Dispatchers.Main) {
                    imageProxy.close()
                }
            }
        }
    }
}

fun ImageProxy.toYUV_420_888Bitmap(): Bitmap {
    val yBuffer = planes[0].buffer  // Y
    val uBuffer = planes[1].buffer  // U
    val vBuffer = planes[2].buffer  // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Copy Y data
    yBuffer.get(nv21, 0, ySize)

    // Copy U and V data interleaved
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}