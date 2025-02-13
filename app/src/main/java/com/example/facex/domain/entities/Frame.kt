package com.example.facex.domain.entities

import android.graphics.Rect
import com.example.facex.domain.helpers.FramePool
import com.example.facex.domain.helpers.FrameProcessor
import com.example.facex.domain.helpers.MemoryPools
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

interface Frame {
    val id: Int
    val buffer: ByteBuffer
    val width: Int
    val height: Int
    val rotationDegrees: Int

    fun duplicate(): Frame
    suspend fun crop(rect: Rect): Frame
    suspend fun rotate(degrees: Int): Frame
    suspend fun alignRotation(): Frame
    suspend fun toGrayscale(): Frame
    fun release()
}

class SharedFrame private constructor(
    override val id: Int,
    private val originalBuffer: ByteBuffer,
    override val width: Int,
    override val height: Int,
    override val rotationDegrees: Int,
    private val pool: FramePool,
    private val refCount: AtomicInteger = AtomicInteger(1),
    private val onRelease: () -> Unit
) : Frame {

    override val buffer: ByteBuffer
        get() = originalBuffer.duplicate()

    override fun duplicate(): Frame {
        refCount.incrementAndGet()
        return this
    }

    override suspend fun crop(rect: Rect): Frame {
        require(rect.left >= 0 && rect.top >= 0 && rect.right <= width && rect.bottom <= height) {
            "Invalid crop rectangle: $rect for frame size ${width}x$height"
        }

        val newBuffer = MemoryPools.acquireByteBuffer(rect.width() * rect.height() * 4)
        return try {
            FrameProcessor.cropBuffer(buffer, width, height, rect, newBuffer)
            create(id = pool.nextId(),
                buffer = newBuffer,
                width = rect.width(),
                height = rect.height(),
                rotationDegrees = rotationDegrees,
                pool = pool,
                onRelease = { MemoryPools.releaseByteBuffer(newBuffer) })
        } catch (e: Exception) {
            MemoryPools.releaseByteBuffer(newBuffer)
            throw e
        }
    }

    override suspend fun rotate(degrees: Int): Frame {
        val normalizedDegrees = degrees % 360
        if (normalizedDegrees == 0) return duplicate()

        val (newWidth, newHeight) = when (normalizedDegrees % 180) {
            0 -> width to height
            else -> height to width
        }

        val newBuffer = MemoryPools.acquireByteBuffer(newWidth * newHeight * 4)
        return try {
            FrameProcessor.rotateBuffer(buffer, width, height, normalizedDegrees, newBuffer)
            create(id = pool.nextId(),
                buffer = newBuffer,
                width = newWidth,
                height = newHeight,
                rotationDegrees = (rotationDegrees + normalizedDegrees) % 360,
                pool = pool,
                onRelease = { MemoryPools.releaseByteBuffer(newBuffer) })
        } catch (e: Exception) {
            MemoryPools.releaseByteBuffer(newBuffer)
            throw e
        }
    }

    override suspend fun alignRotation(): Frame {
        return if (rotationDegrees != 0) {
            rotate(rotationDegrees)
        } else {
            duplicate()
        }
    }

    override suspend fun toGrayscale(): Frame {
        val newBuffer = MemoryPools.acquireByteBuffer(width * height * 4)
        return try {
            FrameProcessor.convertToGrayscale(buffer, width, height, newBuffer)
            create(id = pool.nextId(),
                buffer = newBuffer,
                width = width,
                height = height,
                rotationDegrees = rotationDegrees,
                pool = pool,
                onRelease = { MemoryPools.releaseByteBuffer(newBuffer) })
        } catch (e: Exception) {
            MemoryPools.releaseByteBuffer(newBuffer)
            throw e
        }
    }

    override fun release() {
        if (refCount.decrementAndGet() == 0) {
            onRelease()
            pool.returnFrame(this)
        }
    }

    companion object {
        fun create(
            id: Int,
            buffer: ByteBuffer,
            width: Int,
            height: Int,
            rotationDegrees: Int,
            pool: FramePool,
            onRelease: () -> Unit
        ): SharedFrame = SharedFrame(
            id = id,
            originalBuffer = buffer,
            width = width,
            height = height,
            rotationDegrees = rotationDegrees,
            pool = pool,
            onRelease = onRelease
        )
    }
}
