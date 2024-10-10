package com.example.facex.ui.helpers.processors.formats.yuv420888.converters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.example.facex.domain.entities.ImageInput
import com.example.facex.ui.helpers.models.ImageOutputType
import com.example.facex.ui.helpers.processors.conversion.ImageFormatConversion
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object Yuv420888ToBitmapConverter : ImageFormatConversion {
    override suspend fun convert(image: ImageInput, outputType: ImageOutputType): ImageInput {
        return when (image) {
            is ImageInput.FromImageProxy -> convertFromImageProxy(image, outputType)
            is ImageInput.FromByteBuffer -> convertFromByteBuffer(image, outputType)
            else -> error("Unsupported input type for YUV_420_888 to Bitmap conversion")
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun convertFromImageProxy(
        image: ImageInput.FromImageProxy, outputType: ImageOutputType
    ): ImageInput {
        require(image.imageProxy.format == ImageFormat.YUV_420_888) { "Image must be in YUV_420_888 format" }
        val nv21 = convertYuv420ToNv21(image.imageProxy.image!!, image.width, image.height)
        return processOutput(nv21, image.width, image.height, outputType)
    }

    private fun convertFromByteBuffer(
        image: ImageInput.FromByteBuffer, outputType: ImageOutputType
    ): ImageInput {
        require(image.format == ImageFormat.YUV_420_888) { "Expected YUV_420_888 format" }
        val nv21 = convertYuv420ToNv21(image.buffer, image.width, image.height)
        return processOutput(nv21, image.width, image.height, outputType)
    }

    private fun convertYuv420ToNv21(image: Image, width: Int, height: Int): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)

        val uvPos = ySize
        val vRowStride = image.planes[2].rowStride
        val uRowStride = image.planes[1].rowStride
        val vPixelStride = image.planes[2].pixelStride
        val uPixelStride = image.planes[1].pixelStride

        val vColumn = ByteArray(vRowStride)
        val uColumn = ByteArray(uRowStride)

        for (row in 0 until height / 2) {
            vBuffer.get(vColumn, 0, vRowStride.coerceAtMost(vBuffer.remaining()))
            uBuffer.get(uColumn, 0, uRowStride.coerceAtMost(uBuffer.remaining()))
            var i = 0
            while (i < width) {
                nv21[uvPos + row * width + i] = vColumn[i * vPixelStride]
                nv21[uvPos + row * width + i + 1] = uColumn[i * uPixelStride]
                i += 2
            }
        }

        return nv21
    }

    private fun convertYuv420ToNv21(buffer: ByteBuffer, width: Int, height: Int): ByteArray {
        val size = width * height
        val nv21 = ByteArray(size + (size / 2))
        buffer.position(0)

        // Copy Y plane
        buffer.get(nv21, 0, size)

        // Interleave U and V planes
        val uPos = size
        val vPos = size + 1
        val uvSize = size / 4

        for (i in 0 until uvSize) {
            nv21[uPos + 2 * i] = buffer.get(size + i)
            nv21[vPos + 2 * i] = buffer.get(size + uvSize + i)
        }

        return nv21
    }

    private fun processOutput(
        nv21: ByteArray, width: Int, height: Int, outputType: ImageOutputType
    ): ImageInput {
        return when (outputType) {
            is ImageOutputType.Bitmap ->
                ImageInput.FromBitmap(convertNv21ToBitmap(nv21, width, height))

            is ImageOutputType.ByteArray ->
                ImageInput.FromByteBuffer(ByteBuffer.wrap(nv21), width, height, ImageFormat.NV21)

            is ImageOutputType.ByteBuffer ->
                ImageInput.FromByteBuffer(ByteBuffer.wrap(nv21), width, height, ImageFormat.NV21)
        }
    }

    private fun convertNv21ToBitmap(nv21: ByteArray, width: Int, height: Int): Bitmap {
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}