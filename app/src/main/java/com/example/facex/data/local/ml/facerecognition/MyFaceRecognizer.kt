package com.example.facex.data.local.ml.facerecognition

import android.graphics.Bitmap
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.local.ml.TFLiteModelHandler
import com.example.facex.data.toGrayScale
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyFaceRecognizer @Inject constructor(
    private val tfliteModelHandler: TFLiteModelHandler,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    init {
        tfliteModelHandler.loadModel(MODEL_NAME)
    }

    suspend fun recognizeFaces(
        bitmap: Bitmap,
        rotation: Int,
        faces: List<Face>
    ): List<DetectedFace> =
        withContext(defaultDispatcher) {
            faces.mapNotNull { face ->
                face.processFace(bitmap, rotation)
            }
        }

    private fun Face.processFace(bitmap: Bitmap, rotation: Int): DetectedFace? {
        val croppedBitmap =
            bitmap.cropToBoundingBox(boundingBox, rotation)?.toGrayScale()
        return croppedBitmap?.let {
            DetectedFace(
                boundingBox = boundingBox,
                trackedId = trackingId,
                embedding = calculateEmbeddingFloatArray(it),
                bitmap = it
            )
        }
    }

    private fun calculateEmbeddingFloatArray(imageBitmap: Bitmap): Embedding {
        val byteBuffer = FaceNetBitmapHandler.convertBitmapToByteBuffer(imageBitmap)
        val faceOutputArray = Array(1) { FloatArray(EMBEDDING_SIZE) }
        tfliteModelHandler.runModel(byteBuffer, faceOutputArray)
        return faceOutputArray[0]
    }

    private fun calculateEmbedding(imageBitmap: Bitmap): ByteBuffer {
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
