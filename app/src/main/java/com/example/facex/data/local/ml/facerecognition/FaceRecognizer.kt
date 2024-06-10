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


    fun cosineSimilarity(embeddingA: ByteBuffer, embeddingB: ByteBuffer): Float {
        require(embeddingA.capacity() == embeddingB.capacity()) { "Buffers must have the same dimensions" }

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        embeddingA.rewind()
        embeddingB.rewind()

        while (embeddingA.hasRemaining() && embeddingB.hasRemaining()) {
            val valueA = embeddingA.float
            val valueB = embeddingB.float

            dotProduct += valueA * valueB
            normA += valueA * valueA
            normB += valueB * valueB
        }

        embeddingA.rewind()
        embeddingB.rewind()

        val similarity = dotProduct / (sqrt(normA) * sqrt(normB))
        return similarity.toFloat()
    }

    fun close() {
        tfliteModelHandler.close()
    }

    companion object {
        private const val EMBEDDING_SIZE = 512
        private const val FLOAT_SIZE = 4
    }
}