package com.example.facex.data.local.ml.facerecognition

import android.graphics.Bitmap
import com.example.facex.data.local.ml.TFLiteModelHandler
import com.example.facex.domain.entities.Embedding
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyFaceRecognizer @Inject constructor(
    private val tfliteModelHandler: TFLiteModelHandler,
) {
    init {
        tfliteModelHandler.loadModel(MODEL_NAME)
    }


    fun calculateEmbeddingFloatArray(faceBitmap: Bitmap): Embedding {
        val byteBuffer = FaceNetBitmapHandler.convertBitmapToByteBuffer(faceBitmap)
        val faceOutputArray = Array(1) { FloatArray(EMBEDDING_SIZE) }
        tfliteModelHandler.runModel(byteBuffer, faceOutputArray)
        return faceOutputArray[0]
    }

    private fun calculateEmbeddingByteBuffer(imageBitmap: Bitmap): ByteBuffer {
        val byteBuffer = FaceNetBitmapHandler.convertBitmapToTensorImage(imageBitmap)
        val embeddingsByteBuffer =
            ByteBuffer.allocateDirect(BUFFER_SIZE).order(ByteOrder.nativeOrder())
        tfliteModelHandler.runModel(byteBuffer.buffer, embeddingsByteBuffer)
        embeddingsByteBuffer.rewind()
        return embeddingsByteBuffer
    }

    fun close() {
        tfliteModelHandler.close()
    }

    companion object {
        private const val EMBEDDING_SIZE = 512
        private const val FLOAT_SIZE = 4
        private const val MODEL_NAME = "ms1m_mobilenetv2_16.tflite"
        const val BUFFER_SIZE = EMBEDDING_SIZE * FLOAT_SIZE // 512 * 4 = 2048 bytes
    }
}
