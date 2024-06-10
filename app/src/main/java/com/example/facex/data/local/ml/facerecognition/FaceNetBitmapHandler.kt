package com.example.facex.data.local.ml.facerecognition

import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
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

}