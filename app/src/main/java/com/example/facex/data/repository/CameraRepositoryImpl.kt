package com.example.facex.data.repository

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.LifecycleOwner
import com.example.facex.data.local.camera.CameraManager
import com.example.facex.domain.entities.CameraState
import com.example.facex.domain.repository.CameraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class CameraRepositoryImpl @Inject constructor(private val cameraService: CameraManager) :
    CameraRepository {

    override suspend fun startCamera(lifecycleOwner: LifecycleOwner): Flow<CameraState> =
        cameraService.startCamera(lifecycleOwner)

    override fun setImageAnalyzer(analyzer: ImageAnalysis.Analyzer) =
        cameraService.setImageAnalyzer(analyzer)

    override fun setLinearZoom(scale: Float) = cameraService.setLinearZoom(scale)

    override fun setZoomRatio(ratio: Float) = cameraService.setZoomRatio(ratio)

    override fun getLinearZoom(): StateFlow<Float> = cameraService.linearZoom

    override fun getRatioZoom(): StateFlow<Float> = cameraService.zoomRatio

    override suspend fun switchCamera(lifecycleOwner: LifecycleOwner) =
        cameraService.switchCamera(lifecycleOwner)

    override fun handleTapToFocus(x: Float, y: Float) = cameraService.handleTapToFocus(x, y)

    override fun cleanupCamera() = cameraService.cleanupCamera()
}