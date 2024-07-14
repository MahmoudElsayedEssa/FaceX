package com.example.facex.data.local.camera

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CameraService @Inject constructor(@ApplicationContext private val context: Context) {

    private var cameraSelectorOption = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraProvider: ProcessCameraProvider
    private val cameraExecutor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private val imageCapture = ImageCapture.Builder().build()
    private var videoCapture: VideoCapture<Recorder>? = null

    private val imageAnalysis =
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()


    init {
        Log.d(TAG, "init:CameraService ")
        videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
    }

    fun startCamera(
        preview: PreviewView, lifecycleOwner: LifecycleOwner
    ) {
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(preview, lifecycleOwner)
            }, ContextCompat.getMainExecutor(context)
        )
    }

    fun setImageAnalyzer(analyzer: ImageAnalysis.Analyzer) {
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)
    }

    fun switchCamera() {
        cameraSelectorOption = if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        // Re-bind use cases with the new camera selector
    }

    private fun bindCameraUseCases(
        finderView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    ) {
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraSelectorOption).build()

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysis
            )
            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
            preview
                .setSurfaceProvider(finderView.surfaceProvider)
        } catch (e: Exception) {
            Log.e("CameraManager.TAG", "Use case binding failed", e)
        }
    }

    fun setZoomStateListener(
        lifecycleOwner: LifecycleOwner,
        zoomStateListener: (ZoomState) -> Unit
    ) {
        cameraInfo.zoomState.observe(lifecycleOwner, zoomStateListener)
    }

    fun handlePinchToZoom(detector: ScaleGestureDetector) {
        val currentZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1f
        val delta = detector.scaleFactor
        cameraControl.setZoomRatio(currentZoomRatio * delta)
    }

    fun handleTapToFocus(event: MotionEvent, previewView: PreviewView) {
        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
            previewView.width.toFloat(), previewView.height.toFloat()
        )
        val autoFocusPoint = factory.createPoint(event.x, event.y)
        try {
            cameraControl.startFocusAndMetering(
                FocusMeteringAction.Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(5, TimeUnit.SECONDS)
                    .build()
            )
        } catch (e: CameraInfoUnavailableException) {
            Log.d("CameraXHelper", "Cannot access camera", e)
        }
    }

    fun cleanupCamera() {
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }

    companion object {
        private const val TAG = "CameraService"
    }
}