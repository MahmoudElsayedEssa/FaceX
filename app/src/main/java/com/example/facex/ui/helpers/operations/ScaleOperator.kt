package com.example.facex.ui.helpers.operations

import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.ImageInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class ScaleOperator(private val targetWidth: Int, private val targetHeight: Int) : ImageOperation {
    override suspend fun process(image: ImageInput): ImageInput {
        return withContext(Dispatchers.Default) {
            if (image.width == 0 || image.height == 0) {
                // If the input image has zero width or height, return a blank image of the target size
                return@withContext createBlankImage(image.format)
            }

            if (targetWidth == image.width && targetHeight == image.height) {
                return@withContext image
            }


            when (image) {
                is ImageInput.FromByteBuffer -> scaleFromByteBuffer(image)
                is ImageInput.FromByteArray -> scaleFromByteArray(image)
                is ImageInput.FromBitmap -> scaleFromBitmap(image)
                is ImageInput.FromImageProxy -> scaleFromImageProxy(image)
            }
        }
    }

    private fun scaleFromByteBuffer(image: ImageInput.FromByteBuffer): ImageInput {
        val scaledBuffer = when (image.format) {
            ImageFormat.NV21, ImageFormat.YUV_420_888 -> scaleYUV(
                image.buffer, image.width, image.height
            )

            ImageFormat.RGB_565 -> scaleRGB565(image.buffer, image.width, image.height)
            ImageFormat.FLEX_RGBA_8888 -> scaleRGBA8888(image.buffer, image.width, image.height)
            else -> error("Unsupported format for scaling: ${image.format}")
        }

        return ImageInput.FromByteBuffer(
            buffer = scaledBuffer, width = targetWidth, height = targetHeight, format = image.format
        )
    }

    private fun scaleFromByteArray(image: ImageInput.FromByteArray): ImageInput {
        val scaledBuffer = when (image.format) {
            ImageFormat.NV21, ImageFormat.YUV_420_888 -> scaleYUV(
                ByteBuffer.wrap(image.array), image.width, image.height
            )

            ImageFormat.RGB_565 -> scaleRGB565(
                ByteBuffer.wrap(image.array), image.width, image.height
            )

            ImageFormat.FLEX_RGBA_8888 -> scaleRGBA8888(
                ByteBuffer.wrap(image.array), image.width, image.height
            )

            else -> error("Unsupported format for scaling: ${image.format}")
        }

        return ImageInput.FromByteBuffer(
            buffer = scaledBuffer, width = targetWidth, height = targetHeight, format = image.format
        )
    }

    private fun scaleFromBitmap(image: ImageInput.FromBitmap): ImageInput {
        val scaledBitmap = Bitmap.createScaledBitmap(image.bitmap, targetWidth, targetHeight, true)
        return ImageInput.FromBitmap(scaledBitmap)
    }

    private fun scaleFromImageProxy(image: ImageInput.FromImageProxy): ImageInput {
        val buffer = imageProxyToByteBuffer(image.imageProxy)
        val scaledBuffer = scaleYUV(buffer, image.width, image.height)
        return ImageInput.FromByteBuffer(
            buffer = scaledBuffer,
            width = targetWidth,
            height = targetHeight,
            format = ImageFormat.YUV_420_888
        )
    }

    private fun scaleYUV(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val ySize = width * height
        val uvSize = (width * height) / 4
        val scaled = ByteArray(targetWidth * targetHeight + (targetWidth * targetHeight) / 2)

        // Scale Y plane
        scaleChannel(buffer, 0, width, height, ySize, scaled, 0, targetWidth, targetHeight)

        // Scale U and V planes
        scaleChannel(
            buffer,
            ySize,
            width / 2,
            height / 2,
            uvSize,
            scaled,
            targetWidth * targetHeight,
            targetWidth / 2,
            targetHeight / 2
        )
        scaleChannel(
            buffer,
            ySize + uvSize,
            width / 2,
            height / 2,
            uvSize,
            scaled,
            targetWidth * targetHeight + (targetWidth * targetHeight) / 4,
            targetWidth / 2,
            targetHeight / 2
        )

        return ByteBuffer.wrap(scaled)
    }

    private fun scaleRGB565(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val scaled = ByteArray(targetWidth * targetHeight * 2)
        scaleChannelRGB565(buffer, width, height, scaled, targetWidth, targetHeight)
        return ByteBuffer.wrap(scaled)
    }

    private fun scaleRGBA8888(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val scaled = ByteArray(targetWidth * targetHeight * 4)
        scaleChannelRGBA8888(buffer, width, height, scaled, targetWidth, targetHeight)
        return ByteBuffer.wrap(scaled)
    }

    private fun scaleChannel(
        src: ByteBuffer,
        srcOffset: Int,
        srcWidth: Int,
        srcHeight: Int,
        srcSize: Int,
        dst: ByteArray,
        dstOffset: Int,
        dstWidth: Int,
        dstHeight: Int
    ) {
        val temp = ByteArray(srcSize)
        src.position(srcOffset)
        src.get(temp)

        val xRatio = (srcWidth shl 16) / dstWidth
        val yRatio = (srcHeight shl 16) / dstHeight

        var yy = 0
        for (i in 0 until dstHeight) {
            val y = ((yy shr 16) * srcWidth).coerceAtMost(srcSize - srcWidth)
            var xx = 0
            for (j in 0 until dstWidth) {
                val x = (xx shr 16).coerceAtMost(srcWidth - 1)
                val index = y + x
                dst[dstOffset + i * dstWidth + j] = temp[index]
                xx += xRatio
            }
            yy += yRatio
        }
    }

    private fun scaleChannelRGB565(
        src: ByteBuffer,
        srcWidth: Int,
        srcHeight: Int,
        dst: ByteArray,
        dstWidth: Int,
        dstHeight: Int
    ) {
        val xRatio = (srcWidth shl 16) / dstWidth
        val yRatio = (srcHeight shl 16) / dstHeight

        var yy = 0
        for (i in 0 until dstHeight) {
            val y = ((yy shr 16) * srcWidth).coerceAtMost((srcHeight - 1) * srcWidth)
            var xx = 0
            for (j in 0 until dstWidth) {
                val x = (xx shr 16).coerceAtMost(srcWidth - 1)
                val srcIndex = (y + x) * 2
                val dstIndex = (i * dstWidth + j) * 2
                dst[dstIndex] = src.get(srcIndex)
                dst[dstIndex + 1] = src.get(srcIndex + 1)
                xx += xRatio
            }
            yy += yRatio
        }
    }

    private fun scaleChannelRGBA8888(
        src: ByteBuffer,
        srcWidth: Int,
        srcHeight: Int,
        dst: ByteArray,
        dstWidth: Int,
        dstHeight: Int
    ) {
        val xRatio = (srcWidth shl 16) / dstWidth
        val yRatio = (srcHeight shl 16) / dstHeight

        var yy = 0
        for (i in 0 until dstHeight) {
            val y = ((yy shr 16) * srcWidth).coerceAtMost((srcHeight - 1) * srcWidth)
            var xx = 0
            for (j in 0 until dstWidth) {
                val x = (xx shr 16).coerceAtMost(srcWidth - 1)
                val srcIndex = (y + x) * 4
                val dstIndex = (i * dstWidth + j) * 4
                for (k in 0 until 4) {
                    dst[dstIndex + k] = src.get(srcIndex + k)
                }
                xx += xRatio
            }
            yy += yRatio
        }
    }

    private fun imageProxyToByteBuffer(imageProxy: ImageProxy): ByteBuffer {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteBuffer.allocateDirect(ySize + uSize + vSize)

        nv21.put(yBuffer)
        nv21.put(vBuffer)
        nv21.put(uBuffer)

        nv21.flip()
        return nv21
    }
    private fun createBlankImage(format: Int): ImageInput {
        val size = when (format) {
            ImageFormat.YUV_420_888, ImageFormat.NV21 -> targetWidth * targetHeight * 3 / 2
            ImageFormat.RGB_565 -> targetWidth * targetHeight * 2
            ImageFormat.FLEX_RGBA_8888 -> targetWidth * targetHeight * 4
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
        val buffer = ByteBuffer.allocate(size)
        return ImageInput.FromByteBuffer(buffer, targetWidth, targetHeight, format)
    }
}

