package com.example.facex.ui.helpers.imageprocessing

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue

object ByteBufferPool {
    private const val MAX_POOL_SIZE = 10
    private val pool = ConcurrentLinkedDeque<ByteBuffer>()

    fun getBuffer(size: Int): ByteBuffer {
        synchronized(pool) {
            val buffer = pool.find { it.capacity() >= size }
            if (buffer != null) {
                pool.remove(buffer)
                buffer.clear()
                return buffer
            }
        }
        return ByteBuffer.allocateDirect(size)
    }

    fun returnBuffer(buffer: ByteBuffer) {
        synchronized(pool) {
            if (pool.size < MAX_POOL_SIZE) {
                pool.add(buffer)
            }
        }
    }
}

object BufferPool {
    private val buffers = ConcurrentHashMap<Int, Queue<ByteBuffer>>()

    fun acquire(size: Int): ByteBuffer {
        val queue = buffers.getOrPut(size) { ConcurrentLinkedQueue() }
        return queue.poll() ?: ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
    }

    fun release(buffer: ByteBuffer) {
        buffer.clear()
        buffers.getOrPut(buffer.capacity()) { ConcurrentLinkedQueue() }.offer(buffer)
    }
}