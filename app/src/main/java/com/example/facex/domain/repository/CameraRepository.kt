package com.example.facex.domain.repository

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.LifecycleOwner
import com.example.facex.domain.entities.CameraState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CameraRepository {
    suspend fun startCamera(lifecycleOwner: LifecycleOwner): Flow<CameraState>

    suspend fun switchCamera(lifecycleOwner: LifecycleOwner)

    fun cleanupCamera()

    fun setImageAnalyzer(analyzer: ImageAnalysis.Analyzer)


    fun setLinearZoom(scale: Float)

    fun setZoomRatio(ratio: Float)

    fun getLinearZoom(): StateFlow<Float>

    fun getRatioZoom(): StateFlow<Float>

    fun handleTapToFocus(x: Float, y: Float)

}