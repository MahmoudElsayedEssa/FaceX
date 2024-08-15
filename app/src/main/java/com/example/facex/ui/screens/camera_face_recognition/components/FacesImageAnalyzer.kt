package com.example.facex.ui.screens.camera_face_recognition.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.facex.ui.FrameData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

class FacesImageAnalyzer(
    private val lifecycleScope: CoroutineScope,
    private val onAnalyze: (FrameData) -> Unit,
) : ImageAnalysis.Analyzer {

    var currentFrameId = AtomicLong(0)

    override fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val frameId = currentFrameId.incrementAndGet()

        lifecycleScope.launch(Dispatchers.Default) {
            val currentBitmap = imageProxy.toBitmap()
            try {
                val frameData = FrameData(frameId, currentBitmap, rotationDegrees)
                onAnalyze(frameData)
            } finally {
                withContext(Dispatchers.Main) {
                    imageProxy.close()
                }
            }
        }
    }
}

