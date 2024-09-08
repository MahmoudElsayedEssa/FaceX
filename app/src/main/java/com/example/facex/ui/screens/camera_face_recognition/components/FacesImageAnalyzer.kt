package com.example.facex.ui.screens.camera_face_recognition.components

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.facex.ui.FrameData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

class FacesImageAnalyzer(
    private val lifecycleScope: CoroutineScope,
    private val onAnalyze: suspend (FrameData) -> Unit,
) : ImageAnalysis.Analyzer {

    private var currentFrameId = AtomicLong(0)
    private var lastProcessedTime = AtomicLong(0)
    private val minProcessInterval = 100L // Minimum 100ms between frames

    @OptIn(ExperimentalCoroutinesApi::class)
    private val analysisDispatcher = Dispatchers.Default.limitedParallelism(1)
    private val frameChannel = Channel<ImageProxy>(Channel.CONFLATED)

    init {
        lifecycleScope.launch(analysisDispatcher) {
            for (imageProxy in frameChannel) {
                processFrame(imageProxy)
            }
        }
    }

    override fun analyze(imageProxy: ImageProxy) {
        frameChannel.trySend(imageProxy)
    }

    private suspend fun processFrame(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedTime.get() < minProcessInterval) {
            imageProxy.close()
            return
        }

        lastProcessedTime.set(currentTime)
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val frameId = currentFrameId.incrementAndGet()

        try {
            val currentBitmap = imageProxy.toBitmap()
            val frameData = FrameData(frameId, currentBitmap, rotationDegrees)
            onAnalyze(frameData)
        } finally {
            imageProxy.close()
        }
    }
}

