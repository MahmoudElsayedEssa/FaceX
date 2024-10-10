package com.example.facex.ui.helpers

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.PerformanceTracker
import com.example.facex.ui.FrameData
import com.example.facex.ui.helpers.manager.ImageProcessingManager
import com.example.facex.ui.helpers.models.ImageOutputType
import com.example.facex.ui.helpers.operations.GrayscaleOperator
import com.example.facex.ui.helpers.operations.ScaleOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class FacesImageAnalyzer(
    private val lifecycleScope: CoroutineScope,
    private val onAnalyze: suspend (FrameData) -> Unit,
    private val performanceTracker: PerformanceTracker,
    private val outputType: ImageOutputType = ImageOutputType.ByteBuffer(ImageFormat.NV21)
) : ImageAnalysis.Analyzer {
    private var currentFrameId = AtomicLong(0)

    private val imageProcessingManager = ImageProcessingManager(
        operations = listOf(
            GrayscaleOperator,
            ScaleOperator(480, 360),
        ),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val analysisDispatcher = Dispatchers.Default.limitedParallelism(1)

    override fun analyze(imageProxy: ImageProxy) {
        lifecycleScope.launch(analysisDispatcher) {
            processFrame(imageProxy)
            imageProxy.close()
        }
    }

    private suspend fun processFrame(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val frameId = currentFrameId.incrementAndGet()
        performanceTracker.measureSuspendPerformance(PerformanceTracker.MetricKey.FRAME_PROCESSING_TIME) {

            val processedImage = imageProcessingManager.process(imageProxy, outputType)
            val frameData = FrameData(frameId, rotationDegrees, processedImage)

            onAnalyze(frameData)
        }
    }
}
