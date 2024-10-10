package com.example.facex.domain.entities

import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

    sealed class ImageInput {
        abstract val width: Int
        abstract val height: Int
        abstract val format: Int

        data class FromBitmap(
            val bitmap: Bitmap
        ) : ImageInput() {
            override val width: Int = bitmap.width
            override val height: Int = bitmap.height
            override val format: Int = bitmap.config.toImageFormat()
        }

        data class FromByteArray(
            val array: ByteArray,
            override val width: Int,
            override val height: Int,
            override val format: Int
        ) : ImageInput()

        data class FromByteBuffer(
            val buffer: ByteBuffer,
            override val width: Int,
            override val height: Int,
            override val format: Int
        ) : ImageInput() {
            init {
                require(buffer.capacity() >= width * height * bytesPerPixel(format)) {
                    "Buffer capacity (${buffer.capacity()}) is insufficient for the given dimensions ($width x $height) and format ($format)"
                }
            }
        }

        data class FromImageProxy(val imageProxy: ImageProxy) : ImageInput() {
            override val width: Int = imageProxy.width
            override val height: Int = imageProxy.height
            override val format: Int = imageProxy.format
        }


        companion object {
            fun bytesPerPixel(format: Int): Int = when (format) {
                ImageFormat.NV21, ImageFormat.YUV_420_888 -> 3 / 2
                ImageFormat.RGB_565 -> 2
                ImageFormat.FLEX_RGBA_8888 -> 4
                ImageFormat.PRIVATE -> 1
                else -> error("Unsupported format: $format")
            }
        }
    }

fun Bitmap.Config.toImageFormat(): Int = when (this) {
    Bitmap.Config.RGB_565 -> ImageFormat.RGB_565
    Bitmap.Config.ARGB_8888 -> ImageFormat.FLEX_RGBA_8888
    else -> error("Unsupported Bitmap.Config: $this")
}
