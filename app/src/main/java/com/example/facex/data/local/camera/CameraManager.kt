package com.example.facex.data.local.camera

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.work.await
import com.example.facex.domain.entities.CameraState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors
import javax.inject.Inject


class CameraManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraSelectorOption = CameraSelector.DEFAULT_BACK_CAMERA

    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private val _linearZoom = MutableStateFlow(0f)
    val linearZoom: StateFlow<Float> = _linearZoom.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private var maxZoomRatio = camera?.cameraInfo?.zoomState?.getValue()?.maxZoomRatio

    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner
    ): Flow<CameraState> =
        flow {
            val cameraProvider = cameraProviderFuture.await()
            try {
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelectorOption,
                    preview,
                    imageAnalyzer
                )
                camera?.let { cam ->
                    maxZoomRatio = cam.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                    cam.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                        _zoomRatio.value = state.zoomRatio
                        _linearZoom.value = state.linearZoom
                    }
                }
                emit(CameraState.Active(preview))
            } catch (e: Exception) {
                emit(CameraState.Error(e.toString()))
            }
        }.flowOn(Dispatchers.Main)

    fun setImageAnalyzer(analyzer: ImageAnalysis.Analyzer) {
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }
    }

    suspend fun switchCamera(lifecycleOwner: LifecycleOwner) {
        cameraSelectorOption = if (cameraSelectorOption == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera(lifecycleOwner)
    }

    fun setLinearZoom(zoom: Float) {
        val newLinearZoom = zoom.coerceIn(-1f, 1f)
        _linearZoom.update { newLinearZoom }
        camera?.cameraControl?.setLinearZoom(newLinearZoom)
    }

    fun setZoomRatio(ratio: Float) {
        val newRatio = ratio.coerceIn(1f, maxZoomRatio)
        _zoomRatio.update { newRatio }
        camera?.cameraControl?.setZoomRatio(newRatio)
    }

    fun handleTapToFocus(x: Float, y: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    fun cleanupCamera() {
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }
}

