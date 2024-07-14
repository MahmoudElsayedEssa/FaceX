package com.example.facex.data.local.ml.facerecognition

import android.graphics.Bitmap
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.local.ml.TFLiteModelHandler
import com.example.facex.data.toGrayScale
import com.example.facex.domain.entities.DetectedFace
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceRecognizer @Inject constructor(
    private val tfliteModelHandler: TFLiteModelHandler,
) {
    private val detector: com.google.mlkit.vision.face.FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .enableTracking()
                .build()
        )
    }
    init {
        tfliteModelHandler.loadModel("mobile_face_net.tflite")

    }

    fun detectFacesInImage(bitmap: Bitmap, rotationDegrees: Int): Flow<List<DetectedFace>> = callbackFlow {
        val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val detectedFaces = faces.mapNotNull { face ->
                    bitmap.cropToBoundingBox(face.boundingBox, rotationDegrees)
                        ?.toGrayScale()
                        ?.let { faceBitmap ->
                            DetectedFace(
                                boundingBox = face.boundingBox,
                                trackedId = face.trackingId,
                                embedding = calculateEmbedding(faceBitmap),
                                bitmap = faceBitmap
                            )
                        }
                }
                trySend(detectedFaces).isSuccess
            }
            .addOnFailureListener { exception ->
                close(exception)
            }
        awaitClose { }
    }

    private fun calculateEmbedding(imageBitmap: Bitmap): ByteBuffer {
        val byteBuffer = FaceNetBitmapHandler.convertBitmapToTensorImage(imageBitmap)
        val embeddingsByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(ByteOrder.nativeOrder())
        tfliteModelHandler.runModel(byteBuffer.buffer, embeddingsByteBuffer)
        embeddingsByteBuffer.rewind()
        return embeddingsByteBuffer
    }
    fun stop() {
        tfliteModelHandler.close()
        detector.close()
    }

    companion object {
        private const val EMBEDDING_SIZE = 512
        private const val FLOAT_SIZE = 4
        private const val TAG = "FaceRecognizer"
        const val BUFFER_SIZE = EMBEDDING_SIZE * FLOAT_SIZE // 512 * 4 = 2048 bytes

    }
}