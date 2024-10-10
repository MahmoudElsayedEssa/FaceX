package com.example.facex.ui.helpers.operations

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Paint
import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.ImageInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

object GrayscaleOperator : ImageOperation {
    override suspend fun process(image: ImageInput): ImageInput {
        return withContext(Dispatchers.Default) {
            when (image) {
                is ImageInput.FromByteBuffer -> grayscaleFromByteBuffer(image)
                is ImageInput.FromByteArray -> grayscaleFromByteArray(image)
                is ImageInput.FromBitmap -> grayscaleFromBitmap(image)
                is ImageInput.FromImageProxy -> grayscaleFromImageProxy(image)
            }
        }
    }

    private fun grayscaleFromByteBuffer(image: ImageInput.FromByteBuffer): ImageInput {
        val grayscaledBuffer = when (image.format) {
            ImageFormat.NV21, ImageFormat.YUV_420_888 -> grayscaleYUV(
                image.buffer, image.width, image.height
            )

            ImageFormat.RGB_565 -> grayscaleRGB565(image.buffer, image.width, image.height)
            ImageFormat.FLEX_RGBA_8888 -> grayscaleRGBA8888(image.buffer, image.width, image.height)
            else -> error("Unsupported format for grayscaling: ${image.format}")
        }

        return ImageInput.FromByteBuffer(
            buffer = grayscaledBuffer,
            width = image.width,
            height = image.height,
            format = image.format
        )
    }

    private fun grayscaleFromByteArray(image: ImageInput.FromByteArray): ImageInput {
        val grayscaledBuffer = when (image.format) {
            ImageFormat.NV21, ImageFormat.YUV_420_888 -> grayscaleYUV(
                ByteBuffer.wrap(image.array), image.width, image.height
            )

            ImageFormat.RGB_565 -> grayscaleRGB565(
                ByteBuffer.wrap(image.array), image.width, image.height
            )

            ImageFormat.FLEX_RGBA_8888 -> grayscaleRGBA8888(
                ByteBuffer.wrap(image.array), image.width, image.height
            )

            else -> error("Unsupported format for grayscaling: ${image.format}")
        }

        return ImageInput.FromByteBuffer(
            buffer = grayscaledBuffer,
            width = image.width,
            height = image.height,
            format = image.format
        )
    }

    private fun grayscaleFromBitmap(image: ImageInput.FromBitmap): ImageInput {
        val width = image.bitmap.width
        val height = image.bitmap.height
        val grayscaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(grayscaledBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(image.bitmap, 0f, 0f, paint)

        return ImageInput.FromBitmap(grayscaledBitmap)
    }

    private fun grayscaleFromImageProxy(image: ImageInput.FromImageProxy): ImageInput {
        val buffer = imageProxyToByteBuffer(image.imageProxy)
        val grayscaledBuffer = grayscaleYUV(buffer, image.width, image.height)
        return ImageInput.FromByteBuffer(
            buffer = grayscaledBuffer,
            width = image.width,
            height = image.height,
            format = ImageFormat.YUV_420_888
        )
    }

    private fun grayscaleYUV(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        // YUV is already grayscale in the Y plane, we just need to set U and V to 128 (neutral)
        val ySize = width * height
        val uvSize = (width * height) / 4
        val grayscaled = ByteArray(ySize + uvSize * 2)

        // Copy Y plane (already grayscale)
        buffer.get(grayscaled, 0, ySize)

        // Set U and V planes to 128 (neutral)
        for (i in ySize until grayscaled.size) {
            grayscaled[i] = 128.toByte()
        }

        return ByteBuffer.wrap(grayscaled)
    }

    private fun grayscaleRGB565(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val grayscaled = ByteArray(width * height * 2)
        for (i in 0 until width * height) {
            val pixel = buffer.short
            val r = ((pixel.toInt() and 0xF800) shr 11) shl 3
            val g = ((pixel.toInt() and 0x07E0) shr 5) shl 2
            val b = (pixel.toInt() and 0x001F) shl 3
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            val grayPixel = ((gray shr 3) shl 11) or ((gray shr 2) shl 5) or (gray shr 3)
            grayscaled[i * 2] = (grayPixel and 0xFF).toByte()
            grayscaled[i * 2 + 1] = ((grayPixel shr 8) and 0xFF).toByte()
        }
        return ByteBuffer.wrap(grayscaled)
    }

    private fun grayscaleRGBA8888(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val grayscaled = ByteArray(width * height * 4)
        for (i in 0 until width * height) {
            val r = buffer.get().toInt() and 0xFF
            val g = buffer.get().toInt() and 0xFF
            val b = buffer.get().toInt() and 0xFF
            val a = buffer.get().toInt() and 0xFF
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            grayscaled[i * 4] = gray.toByte()
            grayscaled[i * 4 + 1] = gray.toByte()
            grayscaled[i * 4 + 2] = gray.toByte()
            grayscaled[i * 4 + 3] = a.toByte()
        }
        return ByteBuffer.wrap(grayscaled)
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
}