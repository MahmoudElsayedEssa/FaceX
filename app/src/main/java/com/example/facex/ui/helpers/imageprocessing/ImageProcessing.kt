package com.example.facex.ui.helpers.imageprocessing

import android.graphics.ImageFormat
import com.example.facex.domain.entities.Rectangle
import java.nio.ByteBuffer

object ImageProcessing {
    fun scaleBuffer(
        buffer: ByteBuffer,
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
        format: Int = ImageFormat.NV21,
    ): ByteBuffer {
        val outputSize =
            when (format) {
                ImageFormat.NV21 -> targetWidth * targetHeight * 3 / 2
                ImageFormat.FLEX_RGBA_8888 -> targetWidth * targetHeight * 3
                else -> throw IllegalArgumentException("Unsupported format")
            }

        // Validate input buffer
        val requiredInputSize =
            when (format) {
                ImageFormat.NV21 -> sourceWidth * sourceHeight * 3 / 2
                ImageFormat.FLEX_RGBA_8888 -> sourceWidth * sourceHeight * 3
                else -> throw IllegalArgumentException("Unsupported format")
            }

        if (buffer.capacity() < requiredInputSize) {
            throw IllegalArgumentException("Input buffer too small: ${buffer.capacity()} < $requiredInputSize")
        }

        val output =
            ByteBufferPool.getBuffer(outputSize).apply {
                clear()
                limit(outputSize)
            }

        try {
            when (format) {
                ImageFormat.NV21 ->
                    scaleNV21(
                        buffer,
                        sourceWidth,
                        sourceHeight,
                        output,
                        targetWidth,
                        targetHeight,
                    )
                ImageFormat.FLEX_RGBA_8888 ->
                    scaleRGB(
                        buffer,
                        sourceWidth,
                        sourceHeight,
                        output,
                        targetWidth,
                        targetHeight,
                    )
            }

            return output
        } catch (e: Exception) {
            ByteBufferPool.returnBuffer(output)
            throw e
        }
    }

    private fun scaleNV21(
        input: ByteBuffer,
        srcWidth: Int,
        srcHeight: Int,
        output: ByteBuffer,
        dstWidth: Int,
        dstHeight: Int,
    ) {
        val scaleX = srcWidth.toFloat() / dstWidth
        val scaleY = srcHeight.toFloat() / dstHeight

        // Calculate sizes and validate buffers
        val srcYSize = srcWidth * srcHeight
        val dstYSize = dstWidth * dstHeight

        // Validate buffer sizes
        if (input.capacity() < srcYSize * 3 / 2) {
            throw IllegalArgumentException("Input buffer too small: ${input.capacity()} < ${srcYSize * 3 / 2}")
        }
        if (output.capacity() < dstYSize * 3 / 2) {
            throw IllegalArgumentException("Output buffer too small: ${output.capacity()} < ${dstYSize * 3 / 2}")
        }

        // Reset buffer positions
        input.position(0)
        output.position(0)

        // Process Y plane
        val yRowBuffer = ByteArray(srcWidth)
        val scaledYRow = ByteArray(dstWidth)

        // Scale Y plane row by row to avoid large memory allocations
        for (y in 0 until dstHeight) {
            val srcY = (y * scaleY).toInt().coerceIn(0, srcHeight - 1)

            // Read source row
            input.position(srcY * srcWidth)
            input.get(yRowBuffer, 0, srcWidth)

            // Scale row
            for (x in 0 until dstWidth) {
                val srcX = (x * scaleX).toInt().coerceIn(0, srcWidth - 1)
                scaledYRow[x] = yRowBuffer[srcX]
            }

            // Write scaled row
            output.put(scaledYRow)
        }

        // Process UV plane
        val uvWidth = srcWidth / 2
        val scaledUVWidth = dstWidth / 2
        val uvRowBuffer = ByteArray(srcWidth)
        val scaledUVRow = ByteArray(dstWidth)

        // Scale UV plane row by row
        for (y in 0 until dstHeight / 2) {
            val srcY = (y * scaleY).toInt().coerceIn(0, srcHeight / 2 - 1)

            // Read source UV row
            input.position(srcYSize + srcY * srcWidth)
            input.get(uvRowBuffer, 0, srcWidth)

            // Scale row
            for (x in 0 until scaledUVWidth) {
                val srcX = (x * scaleX).toInt().coerceIn(0, uvWidth - 1) * 2
                scaledUVRow[x * 2] = uvRowBuffer[srcX]
                scaledUVRow[x * 2 + 1] = uvRowBuffer[srcX + 1]
            }

            // Write scaled row
            output.put(scaledUVRow, 0, dstWidth)
        }

        // Prepare output buffer for reading
        output.flip()
    }

    private fun scaleRGB(
        input: ByteBuffer,
        srcWidth: Int,
        srcHeight: Int,
        output: ByteBuffer,
        dstWidth: Int,
        dstHeight: Int,
    ) {
        val scaleX = srcWidth.toFloat() / dstWidth
        val scaleY = srcHeight.toFloat() / dstHeight

        for (y in 0 until dstHeight) {
            for (x in 0 until dstWidth) {
                val srcX = (x * scaleX).toInt().coerceIn(0, srcWidth - 1)
                val srcY = (y * scaleY).toInt().coerceIn(0, srcHeight - 1)

                val srcIndex = (srcY * srcWidth + srcX) * 3
                val dstIndex = (y * dstWidth + x) * 3

                // Copy RGB values
                repeat(3) { i ->
                    output.put(dstIndex + i, input.get(srcIndex + i))
                }
            }
        }
    }

    fun convertToGrayscale(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        format: Int = ImageFormat.NV21,
    ): ByteBuffer {
        val output =
            when (format) {
                ImageFormat.NV21 -> ByteBufferPool.getBuffer(width * height * 3 / 2)
                ImageFormat.FLEX_RGBA_8888 -> ByteBufferPool.getBuffer(width * height * 3)
                else -> throw IllegalArgumentException("Unsupported format")
            }.apply { clear() }

        try {
            when (format) {
                ImageFormat.NV21 -> toGrayscaleNV21(buffer, output, width, height)
                ImageFormat.FLEX_RGBA_8888 -> toGrayscaleRGB(buffer, output, width, height)
            }

            return output.apply { flip() }
        } catch (e: Exception) {
            ByteBufferPool.returnBuffer(output)
            throw e
        }
    }

    private fun toGrayscaleNV21(
        input: ByteBuffer,
        output: ByteBuffer,
        width: Int,
        height: Int,
    ) {
        // Copy Y plane as-is (it's already grayscale)
        val ySize = width * height
        for (i in 0 until ySize) {
            output.put(input.get(i))
        }

        // Fill UV plane with 128 (neutral chroma)
        repeat(ySize / 2) {
            output.put(128.toByte())
        }
    }

    private fun toGrayscaleRGB(
        input: ByteBuffer,
        output: ByteBuffer,
        width: Int,
        height: Int,
    ) {
        val size = width * height

        for (i in 0 until size) {
            val index = i * 3
            val r = input.get(index).toInt() and 0xFF
            val g = input.get(index + 1).toInt() and 0xFF
            val b = input.get(index + 2).toInt() and 0xFF

            // Standard grayscale conversion weights
            val gray = ((0.299 * r) + (0.587 * g) + (0.114 * b)).toInt().toByte()

            // Write gray value to all channels
            output.put(gray)
            output.put(gray)
            output.put(gray)
        }
    }

    fun adjustColors(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        format: Int = ImageFormat.NV21,
        brightness: Float = 1.0f,
        contrast: Float = 1.0f,
    ): ByteBuffer {
        if (brightness == 1.0f && contrast == 1.0f) return buffer

        val size =
            when (format) {
                ImageFormat.NV21 -> width * height * 3 / 2
                ImageFormat.FLEX_RGBA_8888 -> width * height * 3
                else -> throw IllegalArgumentException("Unsupported format")
            }

        val output = ByteBufferPool.getBuffer(size).apply { clear() }

        try {
            when (format) {
                ImageFormat.NV21 ->
                    adjustColorsNV21(
                        buffer,
                        output,
                        width,
                        height,
                        brightness,
                        contrast,
                    )

                ImageFormat.FLEX_RGBA_8888 ->
                    adjustColorsRGB(
                        buffer,
                        output,
                        width,
                        height,
                        brightness,
                        contrast,
                    )
            }

            return output.apply { flip() }
        } catch (e: Exception) {
            ByteBufferPool.returnBuffer(output)
            throw e
        }
    }

    private fun adjustColorsNV21(
        input: ByteBuffer,
        output: ByteBuffer,
        width: Int,
        height: Int,
        brightness: Float,
        contrast: Float,
    ) {
        val ySize = width * height

        // Adjust Y plane for brightness and contrast
        for (i in 0 until ySize) {
            val y = input.get(i).toInt() and 0xFF
            val adjusted = ((y - 128) * contrast + 128) * brightness
            output.put(adjusted.toInt().coerceIn(0, 255).toByte())
        }

        // Copy UV plane as-is
        for (i in 0 until ySize / 2) {
            output.put(input.get(ySize + i))
        }
    }

    private fun adjustColorsRGB(
        input: ByteBuffer,
        output: ByteBuffer,
        width: Int,
        height: Int,
        brightness: Float,
        contrast: Float,
    ) {
        val size = width * height

        for (i in 0 until size) {
            val index = i * 3
            repeat(3) { c ->
                val color = input.get(index + c).toInt() and 0xFF
                val adjusted = ((color - 128) * contrast + 128) * brightness
                output.put(adjusted.toInt().coerceIn(0, 255).toByte())
            }
        }
    }

    fun cropImage(
        buffer: ByteBuffer,
        rect: Rectangle,
        sourceWidth: Int,
        sourceHeight: Int,
        format: Int = ImageFormat.NV21,
    ): ByteBuffer {
        // Ensure rectangle is within bounds
        val x = rect.x.coerceIn(0, sourceWidth - 1)
        val y = rect.y.coerceIn(0, sourceHeight - 1)
        val width = (rect.width).coerceIn(1, sourceWidth - x)
        val height = (rect.height).coerceIn(1, sourceHeight - y)

        val outputSize =
            when (format) {
                ImageFormat.NV21 -> width * height * 3 / 2
                ImageFormat.FLEX_RGBA_8888 -> width * height * 3
                else -> throw IllegalArgumentException("Unsupported format")
            }

        val output = ByteBufferPool.getBuffer(outputSize).apply { clear() }

        try {
            when (format) {
                ImageFormat.NV21 ->
                    cropNV21(
                        input = buffer,
                        output = output,
                        sourceWidth = sourceWidth,
                        sourceHeight = sourceHeight,
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                    )

                ImageFormat.FLEX_RGBA_8888 ->
                    cropRGB(
                        input = buffer,
                        output = output,
                        sourceWidth = sourceWidth,
                        sourceHeight = sourceHeight,
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                    )
            }

            return output.apply { flip() }
        } catch (e: Exception) {
            ByteBufferPool.returnBuffer(output)
            throw e
        }
    }

    private fun cropNV21(
        input: ByteBuffer,
        output: ByteBuffer,
        sourceWidth: Int,
        sourceHeight: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        val sourceYSize = sourceWidth * sourceHeight
        val destYSize = width * height

        // Copy Y plane
        for (row in 0 until height) {
            val sourceOffset = (y + row) * sourceWidth + x
            val destOffset = row * width

            for (col in 0 until width) {
                output.put(destOffset + col, input.get(sourceOffset + col))
            }
        }

        // Copy UV plane
        val uvY = y / 2
        val uvX = x - (x and 1) // Ensure even X for UV plane

        for (row in 0 until height / 2) {
            val sourceOffset = sourceYSize + (uvY + row) * sourceWidth + uvX
            val destOffset = destYSize + row * width

            for (col in 0 until width step 2) {
                // Copy UV pairs
                output.put(destOffset + col, input.get(sourceOffset + col))
                output.put(destOffset + col + 1, input.get(sourceOffset + col + 1))
            }
        }
    }

    private fun cropRGB(
        input: ByteBuffer,
        output: ByteBuffer,
        sourceWidth: Int,
        sourceHeight: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        for (row in 0 until height) {
            val sourceOffset = ((y + row) * sourceWidth + x) * 3
            val destOffset = row * width * 3

            for (col in 0 until width) {
                val sourceIndex = sourceOffset + col * 3
                val destIndex = destOffset + col * 3

                // Copy RGB values
                repeat(3) { i ->
                    output.put(destIndex + i, input.get(sourceIndex + i))
                }
            }
        }
    }
}
