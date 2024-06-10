package com.example.facex.data.local.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


class ImageAnalyzer(
    private val onAnalyze: (bitmap: Bitmap, rotationDegrees: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private lateinit var bitmapBuffer: Bitmap
    override fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (!::bitmapBuffer.isInitialized) {
            bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
            )
        }
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

        onAnalyze(bitmapBuffer, rotationDegrees)
    }


}