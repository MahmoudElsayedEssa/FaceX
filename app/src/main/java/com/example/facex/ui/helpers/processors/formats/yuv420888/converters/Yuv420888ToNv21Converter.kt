package com.example.facex.ui.helpers.processors.formats.yuv420888.converters

import android.graphics.ImageFormat
import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.example.facex.domain.entities.ImageInput
import com.example.facex.ui.helpers.models.ImageOutputType
import com.example.facex.ui.helpers.processors.conversion.ImageFormatConversion
import java.nio.ByteBuffer

//object Yuv420888ToNv21Converter : FormatConversion {
//    override suspend fun convert(image: ImageInput): ImageInput {
//        image as ImageInput.FromByteBuffer
//        require(image.format == ImageFormat.YUV_420_888) { "Image not in YUV_420_888 format" }
//        val ySize = image.width * image.height
//        val uvSize = (image.width * image.height) / 4
//        val nv21 = ByteArray(ySize + uvSize * 2)
//
//        image.buffer.position(0)
//
//        // Copy Y plane
//        image.buffer.get(nv21, 0, ySize)
//
//        // Get U and V data
//        val uBuffer = ByteArray(uvSize)
//        val vBuffer = ByteArray(uvSize)
//        image.buffer.get(uBuffer)
//        image.buffer.get(vBuffer)
//
//        // Interleave U and V
//        var uvIndex = ySize
//        for (i in 0 until uvSize) {
//            nv21[uvIndex++] = vBuffer[i]
//            nv21[uvIndex++] = uBuffer[i]
//        }
//        return image.copy(buffer = ByteBuffer.wrap(nv21), format = ImageFormat.NV21)
//    }
//}


object Yuv420888ToNv21Converter : ImageFormatConversion {
    override suspend fun convert(image: ImageInput, outputType: ImageOutputType): ImageInput {
        return when (image) {
            is ImageInput.FromImageProxy -> convertFromImageProxy(image)
            is ImageInput.FromByteBuffer -> convertFromByteBuffer(image)
            else -> error("Unsupported input type for YUV_420_888 to NV21 conversion")
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun convertFromImageProxy(image: ImageInput.FromImageProxy): ImageInput {
        val nv21Bytes = image.imageProxy.image?.let {
            convertYuv420ToNv21(it, image.width, image.height)
        } ?: error("Unable to access image data from ImageProxy")

        return ImageInput.FromByteBuffer(
            ByteBuffer.wrap(nv21Bytes), image.width, image.height, ImageFormat.NV21
        )
    }

    private fun convertFromByteBuffer(image: ImageInput.FromByteBuffer): ImageInput {
        require(image.format == ImageFormat.YUV_420_888) { "Expected YUV_420_888 format" }
        val nv21Bytes = convertYuv420888ToNv21(image.buffer, image.width, image.height)
        return ImageInput.FromByteBuffer(
            ByteBuffer.wrap(nv21Bytes), image.width, image.height, ImageFormat.NV21
        )
    }

    private fun convertYuv420ToNv21(image: Image, width: Int, height: Int): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val nv21 = ByteArray(ySize + (ySize / 2))

        // Copy Y plane
        yBuffer.get(nv21, 0, ySize)

        val vRowStride = image.planes[2].rowStride
        val uRowStride = image.planes[1].rowStride
        val vPixelStride = image.planes[2].pixelStride
        val uPixelStride = image.planes[1].pixelStride

        val vColumnBuffer = ByteArray(vRowStride)
        val uColumnBuffer = ByteArray(uRowStride)

        for (row in 0 until height / 2) {
            vBuffer.get(vColumnBuffer, 0, vRowStride.coerceAtMost(vBuffer.remaining()))
            uBuffer.get(uColumnBuffer, 0, uRowStride.coerceAtMost(uBuffer.remaining()))

            var uvIndex = ySize + row * width
            for (col in 0 until width / 2) {
                nv21[uvIndex++] = vColumnBuffer[col * vPixelStride]
                nv21[uvIndex++] = uColumnBuffer[col * uPixelStride]
            }
        }

        return nv21
    }

    private fun convertYuv420888ToNv21(buffer: ByteBuffer, width: Int, height: Int): ByteArray {
        val size = width * height
        val nv21 = ByteArray(size + (size / 2))
        buffer.position(0)

        // Copy Y plane
        buffer.get(nv21, 0, size)

        // Extract U and V planes
        val uBuffer = ByteArray(size / 4)
        val vBuffer = ByteArray(size / 4)
        buffer.get(uBuffer)
        buffer.get(vBuffer)

        // Interleave U and V planes
        var uvIndex = size
        for (i in 0 until size / 4) {
            nv21[uvIndex++] = vBuffer[i]
            nv21[uvIndex++] = uBuffer[i]
        }

        return nv21
    }
}