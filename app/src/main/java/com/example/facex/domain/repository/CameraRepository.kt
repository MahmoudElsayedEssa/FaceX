package com.example.facex.domain.repository

import android.view.ScaleGestureDetector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface CameraRepository {
    fun startCamera(preview: PreviewView, lifecycleOwner: LifecycleOwner)

    fun cleanupCamera()

    fun handlePinchToZoom(detector: ScaleGestureDetector)

    fun setImageAnalyzer(analyzer: ImageAnalysis.Analyzer)


}