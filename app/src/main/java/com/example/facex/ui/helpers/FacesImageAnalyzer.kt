package com.example.facex.ui.helpers


import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.helpers.FramePool
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.logger.logWarning
import com.example.facex.domain.performancetracking.PerformanceTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock
import kotlin.coroutines.cancellation.CancellationException


@Singleton
class FacesImageAnalyzer @Inject constructor(
    private val lifecycleScope: CoroutineScope,
    private val framePool: FramePool,
    private val onAnalyze: suspend (Result<Frame>) -> Unit,
    private val performanceTracker: PerformanceTracker,
    private val logger: Logger,
    private val config: AnalysisConfig = AnalysisConfig()
) : ImageAnalysis.Analyzer {

    private var currentProcessing: Job? = null
    private val lastFrame = AtomicReference<Frame?>(null)
    private val lock = ReentrantLock()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val analyzerDispatcher = Dispatchers.Default.limitedParallelism(config.threadPoolSize)

    fun getLastFrame(): Frame? = lastFrame.get()?.duplicate()

    override fun analyze(imageProxy: ImageProxy) {
        currentProcessing?.cancel()

        currentProcessing = lifecycleScope.launch(analyzerDispatcher) {
            try {
                performanceTracker.suspendTrack("Frame Acquisition") {
                    processFrame(imageProxy)
                }
            } catch (e: CancellationException) {
                "Frame processing cancelled".logDebug(logger)
                throw e
            } catch (e: Exception) {
                "Error processing frame: ${e.message}".logWarning(logger)
            } finally {
                imageProxy.close()
            }
        }
    }

    private suspend fun processFrame(imageProxy: ImageProxy) {
        framePool.acquireFrame(imageProxy).use { frame ->
            try {
                updateLastFrame(frame.duplicate())
                onAnalyze(Result.success(frame))
            } catch (e: Exception) {
                "Error in frame processing: ${e.message}".logWarning(logger)
                onAnalyze(Result.failure(e))
            }
        }
    }

    private fun updateLastFrame(newFrame: Frame) {
        lock.withLock {
            lastFrame.get()?.release()
            lastFrame.set(newFrame)
        }
    }

    fun cleanup() {
        currentProcessing?.cancel()
        currentProcessing = null
        lock.withLock {
            lastFrame.get()?.release()
            lastFrame.set(null)
        }
        lifecycleScope.launch {
            framePool.cleanup()
        }
    }

    private inline fun <R> Frame.use(block: (Frame) -> R): R {
        try {
            return block(this)
        } finally {
            release()
        }
    }
}
