package com.example.facex.data.local.ml.facerecognition

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.local.ml.TFLiteModelHandler
import com.example.facex.data.local.ml.facedetection.FaceDetector
import com.example.facex.domain.entities.DetectedFace
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceRecognizer @Inject constructor(
    private val tfliteModelHandler: TFLiteModelHandler,
    private val faceDetector: FaceDetector
) {
    init {
        tfliteModelHandler.loadModel("mobile_face_net.tflite")

    }

    private fun calculateEmbedding(imageBitmap: Bitmap): ByteBuffer {
        val byteBuffer = FaceNetBitmapHandler.convertBitmapToByteBuffer(imageBitmap)

        val embeddingsByteBuffer =
            ByteBuffer.allocateDirect(EMBEDDING_SIZE * FLOAT_SIZE).order(ByteOrder.nativeOrder())

        tfliteModelHandler.runModel(byteBuffer, embeddingsByteBuffer)

        embeddingsByteBuffer.rewind()

        return embeddingsByteBuffer
    }

    fun detectFacesInImage(
        bitmap: Bitmap,
        rotationDegrees: Int,
        onFacesDetected: (detectedFaces: List<DetectedFace>) -> Unit,
    ) {
        faceDetector.detectFacesInImage(bitmap,rotationDegrees).addOnSuccessListener { faces ->
            val detectedFaces = faces.mapNotNull { face ->
                cropToBoundingBox(bitmap, face.boundingBox)?.let { faceBitmap ->
                    val embeddings = calculateEmbedding(faceBitmap)
                    DetectedFace(face.boundingBox, face.trackingId, embeddings)
                }
            }
            onFacesDetected(detectedFaces)
        }
    }


    fun close() {
        tfliteModelHandler.close()
        faceDetector.stop()
    }

    companion object {
        private const val EMBEDDING_SIZE = 512
        private const val FLOAT_SIZE = 4
        private const val TAG = "FaceRecognizer"
    }
}