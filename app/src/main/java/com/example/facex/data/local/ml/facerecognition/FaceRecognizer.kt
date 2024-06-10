package com.example.facex.data.local.ml.facerecognition

import android.graphics.Bitmap
import com.example.facex.data.local.ml.TFLiteModelHandler
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.sqrt


class FaceRecognizer @Inject constructor(
    private val tfliteModelHandler: TFLiteModelHandler
) {

    init {
        tfliteModelHandler.loadModel("mobile_face_net.tflite")

    }

    fun calculateEmbedding(imageBitmap: Bitmap): ByteBuffer {
        val byteBuffer = FaceNetBitmapHandler.convertBitmapToByteBuffer(imageBitmap)

        val embeddingsByteBuffer =
            ByteBuffer.allocateDirect(EMBEDDING_SIZE * FLOAT_SIZE).order(ByteOrder.nativeOrder())

        tfliteModelHandler.runModel(byteBuffer, embeddingsByteBuffer)

        embeddingsByteBuffer.rewind()

        return embeddingsByteBuffer
    }



    fun close() {
        tfliteModelHandler.close()
    }

    companion object {
        private const val EMBEDDING_SIZE = 512
        private const val FLOAT_SIZE = 4
    }
}