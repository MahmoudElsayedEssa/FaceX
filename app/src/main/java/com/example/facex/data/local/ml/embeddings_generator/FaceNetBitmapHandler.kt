package com.example.facex.data.local.ml.embeddings_generator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FaceNetBitmapHandler {
    private const val INPUT_HEIGHT = 112
    private const val INPUT_WIDTH = 112
    private const val IMAGE_MEAN = 127.5f
    private const val IMAGE_STD = 127.5f

    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val resizedFace = Bitmap.createScaledBitmap(
            bitmap, INPUT_HEIGHT, INPUT_WIDTH, true
        )

        val intValues = IntArray(INPUT_HEIGHT * INPUT_WIDTH)

        val imgData = ByteBuffer.allocateDirect(
            4 * INPUT_HEIGHT * INPUT_WIDTH * 3
        ).order(ByteOrder.nativeOrder())

        resizedFace.getPixels(
            intValues, 0, resizedFace.width, 0, 0,
            resizedFace.width, resizedFace.height
        )

        imgData.rewind()
        for (i in 0 until INPUT_HEIGHT) {
            for (j in 0 until INPUT_WIDTH) {
                val index = i * INPUT_HEIGHT + j
                if (index < intValues.size) {
                    val pixelValue = intValues[index]
                    imgData.putFloat((((pixelValue shr 16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat((((pixelValue shr 8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }
        resizedFace.recycle()
        return imgData
    }

    fun convertBitmapToTensorImage(bitmap: Bitmap): TensorImage {
        val tensorImage = TensorImage(DataType.FLOAT32)

        tensorImage.load(bitmap)

        val imageProcessor = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    INPUT_HEIGHT,
                    INPUT_WIDTH,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD)).build()

        return imageProcessor.process(tensorImage)
    }


    fun adaptByteBuffer(yuvBuffer: ByteBuffer, width: Int, height: Int): ByteBuffer {
// Step 1: Convert YUV to Bitmap
        val yuvImage = YuvImage(yuvBuffer.array(), ImageFormat.NV21, width, height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, outputStream)
        val jpegBytes = outputStream.toByteArray()

        // Step 2: Convert JPEG byte array to Bitmap
        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

        // Step 3: Resize Bitmap to INPUT_HEIGHT x INPUT_WIDTH
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)

        // Step 4: Prepare ByteBuffer for the model input
        val imgData = ByteBuffer.allocateDirect(4 * INPUT_HEIGHT * INPUT_WIDTH * 3)
            .order(ByteOrder.nativeOrder())

        // Step 5: Fill the ByteBuffer with normalized pixel values
        val intValues = IntArray(INPUT_HEIGHT * INPUT_WIDTH)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        for (pixelValue in intValues) {
            val r = (pixelValue shr 16 and 0xFF) // Red channel
            val g = (pixelValue shr 8 and 0xFF)  // Green channel
            val b = (pixelValue and 0xFF)        // Blue channel

            // Normalize the pixel values
            imgData.putFloat((r - IMAGE_MEAN) / IMAGE_STD)
            imgData.putFloat((g - IMAGE_MEAN) / IMAGE_STD)
            imgData.putFloat((b - IMAGE_MEAN) / IMAGE_STD)
        }

        // Recycle the bitmap if it's no longer needed
        resizedBitmap.recycle()

        return imgData
    }

}