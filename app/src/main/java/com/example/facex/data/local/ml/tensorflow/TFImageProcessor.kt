package com.example.facex.data.local.ml.tensorflow

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import com.example.facex.data.local.ml.tensorflow.entity.ModelInputImageConfig
import com.example.facex.domain.entities.ImageInput
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


class TFImageProcessor(private val config: ModelInputImageConfig) {
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(config.inputHeight, config.inputWidth, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(config.imageMean, config.imageStd))
        .build()

    fun process(input: ImageInput): ByteBuffer {
        return when (input) {
            is ImageInput.FromBitmap -> processBitmap(input.bitmap)
            is ImageInput.FromByteBuffer -> processByteBuffer(input.buffer, input.width, input.height, input.format)
            is ImageInput.FromByteArray -> processByteArray(input.array, input.width, input.height, input.format)
            is ImageInput.FromImageProxy -> TODO()
        }
    }

    private fun processBitmap(bitmap: Bitmap): ByteBuffer {
        val tensorImage = TensorImage(config.dataType)
        tensorImage.load(bitmap)
        return imageProcessor.process(tensorImage).buffer
    }

    private fun processByteBuffer(buffer: ByteBuffer, width: Int, height: Int, format: Int): ByteBuffer {
        val bitmap = when (format) {
            ImageFormat.NV21, ImageFormat.YUV_420_888 -> yuvToRgbBitmap(buffer, width, height)
            else -> error("Unsupported format: $format")
        }
        return processBitmap(bitmap)
    }

    private fun processByteArray(array: ByteArray, width: Int, height: Int, format: Int): ByteBuffer {
        val buffer = ByteBuffer.wrap(array)
        return processByteBuffer(buffer, width, height, format)
    }

    private fun yuvToRgbBitmap(yuvBuffer: ByteBuffer, width: Int, height: Int): Bitmap {
        val yuvImage = YuvImage(yuvBuffer.array(), ImageFormat.NV21, width, height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, outputStream)
        val jpegBytes = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }
}
