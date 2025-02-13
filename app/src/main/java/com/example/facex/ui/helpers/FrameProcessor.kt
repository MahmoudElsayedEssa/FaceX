package com.example.facex.ui.helpers


import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.facex.domain.logger.Logger
import com.example.facex.ui.screens.camera_face_recognition.FrameData
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

//@OptIn(ExperimentalGetImage::class)
//class FrameProcessor(
//    private val onAnalyze: suspend (Result<FrameData>) -> Unit,
//    private val logger: Logger,
//) {
//    private val isProcessing = AtomicBoolean(false)
//
//    suspend fun processFrame(
//        imageProxy: ImageProxy, frameId: Int, onComplete: (FrameData) -> Unit
//    ) {
//
//        if (!isProcessing.compareAndSet(false, true)) {
//            imageProxy.close()
//            return
//        }
//
//        try {
//            imageProxy.image?.let { image ->
//                val buffer = getBufferFromImage(imageProxy)
//                val frameData = FrameData(
//                    id = frameId,
//                    data = buffer,
//                    rotationDegrees = 0,
//                    width = imageProxy.height,
//                    height = imageProxy.width,
//                )
//
//                try {
//                    onAnalyze(Result.success(frameData))
//                    onComplete(frameData)
//                } finally {
//                    ByteBufferPool.returnBuffer(frameData.data)
//                }
//            }
//
//        } catch (e: Exception) {
//            when (e) {
//                is OutOfMemoryError -> {
//                    ByteBufferPool.clearPool()
//                    System.gc()
//                }
//
//                else -> logger.logError("Frame processing failed", e)
//            }
//            onAnalyze(Result.failure(e))
//        } finally {
//            imageProxy.close()
//            isProcessing.set(false)
//        }
//    }
//
//
//    @OptIn(ExperimentalGetImage::class)
//    private fun getBufferFromImage(imageProxy: ImageProxy): ByteBuffer {
//        val plane = imageProxy.planes[0]
//        val buffer = plane.buffer
//        val rowStride = plane.rowStride
//        val pixelStride = plane.pixelStride
//        val width = imageProxy.width
//        val height = imageProxy.height
//
//        // For 90 degree rotation, swap width and height
//        val bufferSize = width * height * 4
//        val outputBuffer = ByteBufferPool.obtainBuffer()
//        outputBuffer.clear()
//        outputBuffer.limit(bufferSize)
//
//        val rowBuffer = ByteArray(rowStride)
//
//        // Always rotate 90 degrees (read columns as rows)
//        for (col in 0 until width) {
//            for (row in height - 1 downTo 0) {
//                // Get the row data
//                buffer.position(row * rowStride + col * pixelStride)
//                buffer.get(rowBuffer, 0, pixelStride)
//
//                // Write RGBA to ARGB
//                outputBuffer.put(rowBuffer[0])  // R
//                outputBuffer.put(rowBuffer[1])  // G
//                outputBuffer.put(rowBuffer[2])  // B
//                outputBuffer.put(rowBuffer[3])  // A
//            }
//        }
//
//        outputBuffer.position(0)
//        outputBuffer.limit(bufferSize)
//        return outputBuffer
//    }
//}