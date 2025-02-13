package com.example.facex.domain.helpers

import androidx.camera.core.ImageProxy
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.entities.SharedFrame
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
class FramePool @Inject constructor(
    private val logger: Logger,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val lock = ReentrantLock()
    private val frameCounter = AtomicInteger(0)
    private val activeFrames = mutableSetOf<SharedFrame>()

    fun nextId(): Int = frameCounter.incrementAndGet()

    suspend fun acquireFrame(imageProxy: ImageProxy): Frame = withContext(dispatcher) {
        lock.withLock {
            val sourceBuffer = imageProxy.planes[0].buffer
            val bufferSize = sourceBuffer.remaining()

            val frameBuffer = MemoryPools.acquireByteBuffer(bufferSize)

            try {
                frameBuffer.clear()
                frameBuffer.put(sourceBuffer)
                frameBuffer.rewind()
                sourceBuffer.rewind()

                SharedFrame.create(id = nextId(),
                    buffer = frameBuffer,
                    width = imageProxy.width,
                    height = imageProxy.height,
                    rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                    pool = this@FramePool,
                    onRelease = {
                        MemoryPools.releaseByteBuffer(frameBuffer)
                    }).also {
                    activeFrames.add(it)
                    "Created new frame ${it.id}".logDebug(logger)
                }
            } catch (e: Exception) {
                MemoryPools.releaseByteBuffer(frameBuffer)
                throw e
            }
        }
    }

    internal fun returnFrame(frame: SharedFrame) {
        lock.withLock {
            if (activeFrames.remove(frame)) {
                "Released frame ${frame.id}, active frames: ${activeFrames.size}".logDebug(logger)
            }
        }
    }

    fun cleanup() {
        lock.withLock {
            activeFrames.forEach { frame ->
                try {
                    frame.release()
                } catch (e: Exception) {
                    "Error releasing frame ${frame.id}: ${e.message}".logDebug(logger)
                }
            }
            activeFrames.clear()
            MemoryPools.cleanup()
            "Cleaned up all frames".logDebug(logger)
        }
    }

    companion object {
        private const val MAX_ACTIVE_FRAMES = 3
    }
}
