package com.example.facex.ui.helpers.imageprocessing

import androidx.camera.core.ImageProxy.PlaneProxy
import java.nio.ByteBuffer


object ImageConverters {

    fun convertYUV420ToNV21(
        planes: List<PlaneProxy>,
        width: Int,
        height: Int,
        outputBuffer: ByteBuffer? = null,
    ): ByteBuffer {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val totalSize = ySize + (ySize / 2)

        // Reuse or create output buffer
        val result =
            outputBuffer?.apply {
                clear() // Reset position to 0
                if (capacity() < totalSize) {
                    throw IllegalArgumentException("Output buffer capacity (${capacity()}) is too small for required size ($totalSize)")
                }
            } ?: ByteBuffer.allocateDirect(totalSize)

        // Ensure the buffer is in write mode
        result.position(0)
        result.limit(totalSize)

        // Copy Y plane directly to output buffer
        result.put(yBuffer)

        val vRowStride = planes[2].rowStride
        val uRowStride = planes[1].rowStride
        val vPixelStride = planes[2].pixelStride
        val uPixelStride = planes[1].pixelStride

        // Reusable temporary buffers
        val vColumnBuffer = ByteArray(vRowStride)
        val uColumnBuffer = ByteArray(uRowStride)
        val uvRow = ByteArray(width) // Buffer for one UV row

        // Process U and V planes
        for (row in 0 until height / 2) {
            // Read one row from each plane
            vBuffer.get(vColumnBuffer, 0, vRowStride.coerceAtMost(vBuffer.remaining()))
            uBuffer.get(uColumnBuffer, 0, uRowStride.coerceAtMost(uBuffer.remaining()))

            // Interleave U and V samples into the row buffer
            var uvIndex = 0
            for (col in 0 until width / 2) {
                uvRow[uvIndex++] = vColumnBuffer[col * vPixelStride]
                uvRow[uvIndex++] = uColumnBuffer[col * uPixelStride]
            }

            // Write the interleaved row to the output buffer
            result.put(uvRow, 0, width)
        }

        // Prepare buffer for reading
        result.flip()

        return result
    }


    fun convertYUV420ToRGB(
        planes: List<PlaneProxy>,
        width: Int,
        height: Int,
        outputBuffer: ByteBuffer? = null,
    ): ByteBuffer {
        val size = width * height * 3
        // Reuse outputBuffer if provided, otherwise allocate a new one.
        val output = outputBuffer ?: ByteBuffer.allocateDirect(size)
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        // Create arrays to hold Y, U, and V values.
        val y = ByteArray(width * height)
        val u = ByteArray(width * height / 4)
        val v = ByteArray(width * height / 4)

        // Fill the arrays with data from the buffers.
        yBuffer.get(y)
        uBuffer.get(u)
        vBuffer.get(v)

        var yIndex = 0
        var uvIndex = 0

        // Loop through each pixel in the image, converting YUV to RGB.
        for (j in 0 until height) {
            for (i in 0 until width) {
                val yValue = y[yIndex].toInt() and 0xff
                val uValue = u[uvIndex].toInt() and 0xff - 128
                val vValue = v[uvIndex].toInt() and 0xff - 128

                // YUV to RGB conversion formula
                var r = yValue + (1.370705f * vValue)
                var g = yValue - (0.698001f * vValue) - (0.337633f * uValue)
                var b = yValue + (1.732446f * uValue)

                // Clamp RGB values to the range [0, 255]
                r = r.coerceIn(0f, 255f)
                g = g.coerceIn(0f, 255f)
                b = b.coerceIn(0f, 255f)

                // Write the RGB values to the output buffer
                output.put(r.toInt().toByte())
                output.put(g.toInt().toByte())
                output.put(b.toInt().toByte())

                // Increment the Y index, and update the UV index every 2 pixels
                yIndex++
                if (i % 2 == 1 && j % 2 == 1) {
                    uvIndex++
                }
            }
        }

        // Flip the buffer before returning to make it ready for reading.
        return output.apply { flip() }
    }

    fun convertNV21ToRGB8888(
        nv21Buffer: ByteBuffer,
        width: Int,
        height: Int,
        outputBuffer: ByteBuffer? = null,
    ): ByteBuffer {
        val size = width * height * 4 // 4 bytes per pixel for RGBA
        val output = outputBuffer ?: ByteBuffer.allocateDirect(size)

        // Create a byte array to hold the NV21 bytes (YUV420 format)
        val nv21Bytes = ByteArray(width * height * 3 / 2)
        nv21Buffer.get(nv21Bytes)

        var yIndex = 0
        var uvIndex = width * height

        // Loop through each pixel, converting NV21 to RGB8888 (with alpha = 255).
        for (j in 0 until height) {
            for (i in 0 until width) {
                val y = (nv21Bytes[yIndex].toInt() and 0xff).toFloat()
                val v = (nv21Bytes[uvIndex + (i / 2) * 2].toInt() and 0xff) - 128
                val u = (nv21Bytes[uvIndex + (i / 2) * 2 + 1].toInt() and 0xff) - 128

                // YUV to RGB conversion formula
                var r = y + 1.402f * v
                var g = y - 0.344f * u - 0.714f * v
                var b = y + 1.772f * u

                // Clamp RGB values to the range [0, 255]
                r = r.coerceIn(0f, 255f)
                g = g.coerceIn(0f, 255f)
                b = b.coerceIn(0f, 255f)

                // Write RGBA values (with alpha = 255)
                output.put(r.toInt().toByte())
                output.put(g.toInt().toByte())
                output.put(b.toInt().toByte())
                output.put(255.toByte()) // Alpha set to 255

                // Increment the Y index for the next pixel
                yIndex++
            }
            // Update UV index every 2 pixels in each row
            if (j % 2 == 1) {
                uvIndex += width
            }
        }

        // Flip the buffer before returning to make it ready for reading.
        return output.apply { flip() }
    }
}
