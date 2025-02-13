package com.example.facex.ui.helpers

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentLinkedQueue


object ByteBufferPool {
    private val pool = ConcurrentLinkedQueue<ByteBuffer>()
    private const val MAX_POOL_SIZE = 3
    private const val RGBA_BYTES = 4  // RGBA is 4 bytes per pixel
    private const val ANALYZER_WIDTH = 640
    private const val ANALYZER_HEIGHT = 480

    init {
        repeat(MAX_POOL_SIZE) {
            // Allocate for RGBA format (width * height * 4 bytes per pixel)
            pool.offer(ByteBuffer.allocateDirect(ANALYZER_WIDTH * ANALYZER_HEIGHT * RGBA_BYTES).apply {
                order(ByteOrder.nativeOrder())
            })
        }
    }

    fun obtainBuffer(): ByteBuffer = pool.poll()?.apply { clear() }
        ?: ByteBuffer.allocateDirect(ANALYZER_WIDTH * ANALYZER_HEIGHT * RGBA_BYTES).apply {
            order(ByteOrder.nativeOrder())
        }

    fun returnBuffer(buffer: ByteBuffer) {
        if (pool.size < MAX_POOL_SIZE) {
            buffer.clear()
            pool.offer(buffer)
        }
    }

    fun clearPool() {
        pool.forEach { it.clear() }
        pool.clear()
    }
}
