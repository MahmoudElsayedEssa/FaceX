package com.example.facex.domain.helpers

import org.opencv.core.Mat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object MemoryPools {
    private val lock = ReentrantLock()
    private val byteArrayPools = mutableMapOf<Int, ArrayDeque<ByteArray>>()
    private val byteBufferPools = mutableMapOf<Int, ArrayDeque<ByteBuffer>>()
    private val matPool = ArrayDeque<Mat>(5)

    fun acquireByteArray(size: Int): ByteArray = lock.withLock {
        byteArrayPools.getOrPut(size) { ArrayDeque(3) }.removeFirstOrNull() ?: ByteArray(size)
    }

    fun releaseByteArray(array: ByteArray) = lock.withLock {
        val pool = byteArrayPools.getOrPut(array.size) { ArrayDeque(3) }
        if (pool.size < 3) pool.addLast(array)
    }

    fun acquireByteBuffer(size: Int): ByteBuffer = lock.withLock {
        byteBufferPools.getOrPut(size) { ArrayDeque(3) }.removeFirstOrNull()
            ?: ByteBuffer.allocateDirect(size).apply {
                order(ByteOrder.nativeOrder())
            }
    }

    fun releaseByteBuffer(buffer: ByteBuffer) = lock.withLock {
        val pool = byteBufferPools.getOrPut(buffer.capacity()) { ArrayDeque(3) }
        if (pool.size < 3) {
            buffer.clear()
            pool.addLast(buffer)
        }
    }

    fun acquireMat(): Mat = lock.withLock {
        matPool.removeFirstOrNull() ?: Mat()
    }

    fun releaseMat(mat: Mat) = lock.withLock {
        if (matPool.size < 5) {
            mat.release()
            matPool.addLast(mat)
        } else {
            mat.release()
        }
    }

    fun cleanup() = lock.withLock {
        byteArrayPools.clear()
        byteBufferPools.values.forEach { pool ->
            pool.clear()
        }
        byteBufferPools.clear()
        matPool.forEach { it.release() }
        matPool.clear()
    }
}
