package com.example.facex.domain.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap

class RGBA8888Converter {
    private val nv21Buffers = ConcurrentHashMap<Long, Nv21BufferCache>()

    fun convertRgbaToNv21(rgbaBuffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        // Use single long key instead of Pair for less overhead
        val key = (width.toLong() shl 32) or height.toLong()

        // Get or create buffer cache
        val bufferCache = nv21Buffers.getOrPut(key) {
            Nv21BufferCache(width, height)
        }

        if (rgbaBuffer.position() != 0) rgbaBuffer.position(0)
        if (bufferCache.nv21Buffer.position() != 0) bufferCache.nv21Buffer.position(0)

        nativeRgbaToNv21(
            rgbaBuffer = rgbaBuffer,  // Pass ByteBuffer directly
            width = width, height = height, outNv21 = bufferCache.nv21Buffer
        )

        return bufferCache.nv21Buffer
    }

    private class Nv21BufferCache(width: Int, height: Int) {
        val nv21Buffer: ByteBuffer =
            ByteBuffer.allocateDirect((width * height * 3f / 2f).toInt()).apply {
                order(ByteOrder.nativeOrder())
            }
    }

    private external fun nativeRgbaToNv21(
        rgbaBuffer: ByteBuffer, width: Int, height: Int, outNv21: ByteBuffer
    )

    companion object {
        init {
            System.loadLibrary("yuv_converter")
        }
    }
}
