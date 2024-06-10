package com.example.facex.data.local.ml.facerecognition

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.local.ml.TFLiteModelHandler
import com.example.facex.data.local.ml.facedetection.FaceDetector
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

    fun calculateEmbedding(imageBitmap: Bitmap): ByteBuffer {
        val byteBuffer = FaceNetBitmapHandler.convertBitmapToByteBuffer(imageBitmap)

        val embeddingsByteBuffer =
            ByteBuffer.allocateDirect(EMBEDDING_SIZE * FLOAT_SIZE).order(ByteOrder.nativeOrder())

        tfliteModelHandler.runModel(byteBuffer, embeddingsByteBuffer)

        embeddingsByteBuffer.rewind()

        return embeddingsByteBuffer
    }

    fun detectFacesInImage(
        bitmap: Bitmap, onFaceDetected: (embedding: ByteBuffer, boundingBox: Rect) -> Unit
    ) {
        faceDetector.detectFacesInImage(bitmap).addOnSuccessListener { faces ->
            faces.mapNotNull { face ->
                cropToBoundingBox(bitmap, face.boundingBox)?.let { faceBitmap ->
                    val embeddings = calculateEmbedding(faceBitmap)
                    onFaceDetected(embeddings, face.boundingBox)
                }
            }
        }
    }


    fun recognizeFaces(bitmap: Bitmap, onRecognizeFace: (embedding: ByteBuffer) -> Unit) {
        detectFacesInImage(bitmap) { embedding, _ ->
            onRecognizeFace(embedding)
        }

    }

    fun close() {
        tfliteModelHandler.close()
        faceDetector.stop()
    }

    companion object {
        private const val EMBEDDING_SIZE = 512
        private const val FLOAT_SIZE = 4
    }
}