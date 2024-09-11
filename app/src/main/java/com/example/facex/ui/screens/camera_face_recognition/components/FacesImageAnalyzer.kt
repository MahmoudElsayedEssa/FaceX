package com.example.facex.ui.screens.camera_face_recognition.components

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.facex.ui.FrameData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlin.system.measureNanoTime


class FacesImageAnalyzer @Inject constructor(
    private val lifecycleScope: CoroutineScope,
    private val onAnalyze: suspend (FrameData) -> Unit
) : ImageAnalysis.Analyzer {


    private var currentFrameId = AtomicLong(0)
    private var lastProcessedTime = AtomicLong(0)

    @OptIn(ExperimentalCoroutinesApi::class)
//    private val analysisDispatcher = Dispatchers.Default.limitedParallelism(1)
//    private val analysisDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val analysisDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    private val frameChannel = Channel<ImageProxy>(Channel.CONFLATED)

    init {
        lifecycleScope.launch(analysisDispatcher) {
            for (imageProxy in frameChannel) {
                launch{  processFrame(imageProxy)}
            }
        }
    }

    val formatter = NumberFormat.getNumberInstance(Locale.US)
    @OptIn(DelicateCoroutinesApi::class)
    override fun analyze(imageProxy: ImageProxy) {
        val analyzeTime = measureNanoTime {
                frameChannel.trySend(imageProxy)
        }
        Log.d("performanceTracker", "analyze:FRAME_ENQUEUE_TIME:${formatter.format(analyzeTime)} ")
    }

    private suspend fun processFrame(imageProxy: ImageProxy) {

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val frameId = currentFrameId.incrementAndGet()
        try {
            val processingTime = measureNanoTime {
                var currentBitmap: Bitmap

                val conversionTime = measureNanoTime {
                    withContext(Dispatchers.Default) {
                        currentBitmap = async { imageProxy.toBitmap() }.await()
                    }
                }
                Log.d(
                    "performanceTracker",
                    "analyze:conversionTime:${formatter.format(conversionTime)} "
                )

                val frameData = FrameData(frameId, currentBitmap, rotationDegrees)
                onAnalyze(frameData)
            }
            Log.d(
                "performanceTracker",
                "analyze:processingTime:${formatter.format(processingTime)} "
            )

        } finally {
            imageProxy.close()
        }
    }

}
