package com.example.facex.domain.repository

import android.graphics.Bitmap
import android.view.ScaleGestureDetector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.facex.data.local.camera.ImageAnalyzer

interface CameraRepository {
    fun startCamera(preview: PreviewView, lifecycleOwner: LifecycleOwner)

    fun cleanupCamera()

    fun handlePinchToZoom(detector: ScaleGestureDetector)

    fun setImageAnalyzer(analyzer: ImageAnalysis.Analyzer)

    fun createImageAnalyzer(
        onAnalyze: (bitmap: Bitmap, rotationDegrees: Int) -> Unit
    ): ImageAnalyzer
}