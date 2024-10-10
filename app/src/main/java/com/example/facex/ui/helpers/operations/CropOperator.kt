package com.example.facex.ui.helpers.operations

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.ImageInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class CropOperator(private val cropRect: Rect) : ImageOperation {
    override suspend fun process(image: ImageInput): ImageInput {
        return withContext(Dispatchers.Default) {
            // Ensure the crop rectangle is valid
            if (cropRect.intersect(Rect(0, 0, image.width, image.height))) {
                // If the crop rectangle is invalid, return the original image
                return@withContext image
            }
            when (image) {
                is ImageInput.FromByteBuffer -> cropFromByteBuffer(image)
                is ImageInput.FromByteArray -> cropFromByteArray(image)
                is ImageInput.FromBitmap -> cropFromBitmap(image)
                is ImageInput.FromImageProxy -> cropFromImageProxy(image)
            }
        }
    }

    private fun cropFromByteBuffer(image: ImageInput.FromByteBuffer): ImageInput {
        val croppedBuffer = when (image.format) {
            ImageFormat.NV21, ImageFormat.YUV_420_888 -> cropYUV(
                image.buffer,
                image.width,
                image.height
            )

            ImageFormat.RGB_565 -> cropRGB565(image.buffer, image.width, image.height)
            ImageFormat.FLEX_RGBA_8888 -> cropRGBA8888(image.buffer, image.width, image.height)
            else -> error("Unsupported format for cropping: ${image.format}")
        }

        return ImageInput.FromByteBuffer(
            buffer = croppedBuffer,
            width = cropRect.width(),
            height = cropRect.height(),
            format = image.format
        )
    }

    private fun cropFromByteArray(image: ImageInput.FromByteArray): ImageInput {
        val croppedBuffer = when (image.format) {
            ImageFormat.NV21, ImageFormat.YUV_420_888 -> cropYUV(
                ByteBuffer.wrap(image.array),
                image.width,
                image.height
            )

            ImageFormat.RGB_565 -> cropRGB565(
                ByteBuffer.wrap(image.array),
                image.width,
                image.height
            )

            ImageFormat.FLEX_RGBA_8888 -> cropRGBA8888(
                ByteBuffer.wrap(image.array),
                image.width,
                image.height
            )

            else -> error("Unsupported format for cropping: ${image.format}")
        }

        return ImageInput.FromByteBuffer(
            buffer = croppedBuffer,
            width = cropRect.width(),
            height = cropRect.height(),
            format = image.format
        )
    }

    private fun cropFromBitmap(image: ImageInput.FromBitmap): ImageInput {
        val croppedBitmap = Bitmap.createBitmap(
            image.bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height()
        )
        return ImageInput.FromBitmap(croppedBitmap)
    }

    private fun cropFromImageProxy(image: ImageInput.FromImageProxy): ImageInput {
        val buffer = imageProxyToByteBuffer(image.imageProxy)
        val croppedBuffer = cropYUV(buffer, image.width, image.height)
        return ImageInput.FromByteBuffer(
            buffer = croppedBuffer,
            width = cropRect.width(),
            height = cropRect.height(),
            format = ImageFormat.YUV_420_888
        )
    }

    private fun cropYUV(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val ySize = width * height
        val uvSize = (width * height) / 4
        val croppedYSize = cropRect.width() * cropRect.height()
        val croppedUVSize = (cropRect.width() * cropRect.height()) / 4

        val croppedBuffer = ByteBuffer.allocate(croppedYSize + 2 * croppedUVSize)

        // Crop Y plane
        for (y in cropRect.top until cropRect.bottom) {
            val startIndex = y * width + cropRect.left
            val rowLength = cropRect.width()
            buffer.position(startIndex)
            val row = ByteArray(rowLength)
            buffer.get(row)
            croppedBuffer.put(row)
        }

        // Crop U and V planes
        val uvWidth = width / 2
        val uvHeight = height / 2
        val croppedUVWidth = cropRect.width() / 2
        val croppedUVHeight = cropRect.height() / 2

        for (plane in 1..2) {
            buffer.position(ySize + (plane - 1) * uvSize)
            for (y in cropRect.top / 2 until cropRect.bottom / 2) {
                val startIndex = y * uvWidth + cropRect.left / 2
                buffer.position(ySize + (plane - 1) * uvSize + startIndex)
                val row = ByteArray(croppedUVWidth)
                buffer.get(row)
                croppedBuffer.put(row)
            }
        }

        croppedBuffer.rewind()
        return croppedBuffer
    }

    private fun cropRGB565(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val croppedBuffer = ByteBuffer.allocate(cropRect.width() * cropRect.height() * 2)

        for (y in cropRect.top until cropRect.bottom) {
            val startIndex = (y * width + cropRect.left) * 2
            buffer.position(startIndex)
            val row = ByteArray(cropRect.width() * 2)
            buffer.get(row)
            croppedBuffer.put(row)
        }

        croppedBuffer.rewind()
        return croppedBuffer
    }

    private fun cropRGBA8888(buffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val croppedBuffer = ByteBuffer.allocate(cropRect.width() * cropRect.height() * 4)

        for (y in cropRect.top until cropRect.bottom) {
            val startIndex = (y * width + cropRect.left) * 4
            buffer.position(startIndex)
            val row = ByteArray(cropRect.width() * 4)
            buffer.get(row)
            croppedBuffer.put(row)
        }

        croppedBuffer.rewind()
        return croppedBuffer
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