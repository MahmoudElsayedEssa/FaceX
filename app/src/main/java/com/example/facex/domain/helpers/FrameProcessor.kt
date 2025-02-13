package com.example.facex.domain.helpers

import android.graphics.Rect
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

object FrameProcessor {
    fun cropBuffer(
        sourceBuffer: ByteBuffer, width: Int, height: Int, rect: Rect, destBuffer: ByteBuffer
    ) {
        val byteArray = MemoryPools.acquireByteArray(sourceBuffer.remaining())
        val sourceMat = MemoryPools.acquireMat()

        try {
            // Copy to Mat
            sourceBuffer.position(0)
            sourceBuffer.get(byteArray)
            sourceMat.create(height, width, CvType.CV_8UC4)
            sourceMat.put(0, 0, byteArray)

            // Crop
            val roi = org.opencv.core.Rect(
                rect.left, rect.top, rect.width(), rect.height()
            )
            val croppedMat = Mat(sourceMat, roi)

            // Copy to destination buffer
            val resultArray =
                MemoryPools.acquireByteArray(croppedMat.total().toInt() * croppedMat.channels())
            try {
                croppedMat.get(0, 0, resultArray)
                destBuffer.clear()
                destBuffer.put(resultArray)
                destBuffer.rewind()
            } finally {
                MemoryPools.releaseByteArray(resultArray)
            }
            croppedMat.release()

        } finally {
            MemoryPools.releaseByteArray(byteArray)
            MemoryPools.releaseMat(sourceMat)
        }
    }

    fun rotateBuffer(
        sourceBuffer: ByteBuffer, width: Int, height: Int, degrees: Int, destBuffer: ByteBuffer
    ) {
        val byteArray = MemoryPools.acquireByteArray(sourceBuffer.remaining())
        val sourceMat = MemoryPools.acquireMat()
        val rotatedMat = MemoryPools.acquireMat()

        try {
            // Copy to Mat
            sourceBuffer.position(0)
            sourceBuffer.get(byteArray)
            sourceMat.create(height, width, CvType.CV_8UC4)
            sourceMat.put(0, 0, byteArray)

            // Rotate
            when (degrees) {
                90 -> Core.rotate(sourceMat, rotatedMat, Core.ROTATE_90_CLOCKWISE)
                180 -> Core.rotate(sourceMat, rotatedMat, Core.ROTATE_180)
                270 -> Core.rotate(sourceMat, rotatedMat, Core.ROTATE_90_COUNTERCLOCKWISE)
                else -> throw IllegalArgumentException("Only 90-degree rotations supported")
            }

            // Copy to destination buffer
            val resultArray =
                MemoryPools.acquireByteArray(rotatedMat.total().toInt() * rotatedMat.channels())
            try {
                rotatedMat.get(0, 0, resultArray)
                destBuffer.clear()
                destBuffer.put(resultArray)
                destBuffer.rewind()
            } finally {
                MemoryPools.releaseByteArray(resultArray)
            }

        } finally {
            MemoryPools.releaseByteArray(byteArray)
            MemoryPools.releaseMat(sourceMat)
            MemoryPools.releaseMat(rotatedMat)
        }
    }

    fun convertToGrayscale(
        sourceBuffer: ByteBuffer, width: Int, height: Int, destBuffer: ByteBuffer
    ) {
        val byteArray = MemoryPools.acquireByteArray(sourceBuffer.remaining())
        val sourceMat = MemoryPools.acquireMat()
        val grayMat = MemoryPools.acquireMat()
        val resultMat = MemoryPools.acquireMat()

        try {
            // Copy to Mat
            sourceBuffer.position(0)
            sourceBuffer.get(byteArray)
            sourceMat.create(height, width, CvType.CV_8UC4)
            sourceMat.put(0, 0, byteArray)

            // Convert to grayscale
            Imgproc.cvtColor(sourceMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

            // Create RGBA result
            val alphaChannel = MemoryPools.acquireMat()
            try {
                alphaChannel.create(height, width, CvType.CV_8UC1)
                alphaChannel.setTo(Scalar(255.0))

                val channels = ArrayList<Mat>(4)
                repeat(3) { channels.add(grayMat) }
                channels.add(alphaChannel)

                Core.merge(channels, resultMat)

                // Copy to destination buffer
                val resultArray =
                    MemoryPools.acquireByteArray(resultMat.total().toInt() * resultMat.channels())
                try {
                    resultMat.get(0, 0, resultArray)
                    destBuffer.clear()
                    destBuffer.put(resultArray)
                    destBuffer.rewind()
                } finally {
                    MemoryPools.releaseByteArray(resultArray)
                }

            } finally {
                MemoryPools.releaseMat(alphaChannel)
            }

        } finally {
            MemoryPools.releaseByteArray(byteArray)
            MemoryPools.releaseMat(sourceMat)
            MemoryPools.releaseMat(grayMat)
            MemoryPools.releaseMat(resultMat)
        }
    }

    fun convertToYUV(
        sourceBuffer: ByteBuffer, width: Int, height: Int, destBuffer: ByteBuffer
    ) {
        val byteArray = MemoryPools.acquireByteArray(sourceBuffer.remaining())
        val sourceMat = MemoryPools.acquireMat()
        val yuvMat = MemoryPools.acquireMat()

        try {
            // Copy to Mat
            sourceBuffer.position(0)
            sourceBuffer.get(byteArray)
            sourceMat.create(height, width, CvType.CV_8UC4)
            sourceMat.put(0, 0, byteArray)

            // Convert RGBA to YUV
            Imgproc.cvtColor(sourceMat, yuvMat, Imgproc.COLOR_RGBA2YUV_YV12)

            // Copy to destination buffer
            val resultArray =
                MemoryPools.acquireByteArray(yuvMat.total().toInt() * yuvMat.channels())
            try {
                yuvMat.get(0, 0, resultArray)
                destBuffer.clear()
                destBuffer.put(resultArray)
                destBuffer.rewind()
            } finally {
                MemoryPools.releaseByteArray(resultArray)
            }

        } finally {
            MemoryPools.releaseByteArray(byteArray)
            MemoryPools.releaseMat(sourceMat)
            MemoryPools.releaseMat(yuvMat)
        }
    }
}