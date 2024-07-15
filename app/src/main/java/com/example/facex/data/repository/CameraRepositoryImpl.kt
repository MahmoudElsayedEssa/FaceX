package com.example.facex.data.repository

import android.view.ScaleGestureDetector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.facex.data.local.camera.CameraService
import com.example.facex.domain.repository.CameraRepository
import javax.inject.Inject

class CameraRepositoryImpl @Inject constructor(
    private val camera: CameraService,
) : CameraRepository {
    override fun startCamera(preview: PreviewView, lifecycleOwner: LifecycleOwner) {
        camera.startCamera(preview = preview, lifecycleOwner = lifecycleOwner)
    }

    override fun cleanupCamera() {
        camera.cleanupCamera()
    }

    override fun handlePinchToZoom(detector: ScaleGestureDetector) {
        camera.handlePinchToZoom(detector)
    }

    override fun setImageAnalyzer(analyzer: ImageAnalysis.Analyzer) {
        camera.setImageAnalyzer(analyzer)
    }
}