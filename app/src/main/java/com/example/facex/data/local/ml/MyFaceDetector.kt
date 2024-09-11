package com.example.facex.data.local.ml


import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MyFaceDetector @Inject constructor() {
    private val detector: com.google.mlkit.vision.face.FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()
        )
    }

    fun detectFaces(bitmap: Bitmap, rotationDegrees: Int, callback: (List<Face>) -> Unit) {
        val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                callback(faces)
            }
            .addOnFailureListener { _ ->
            }
    }

    suspend fun detectFaces(bitmap: Bitmap, rotationDegrees: Int): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
        return suspendCancellableCoroutine { continuation ->
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    continuation.resume(faces)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
                .addOnCanceledListener {
                    continuation.cancel()
                }
        }
    }

    suspend fun detectFacesSuspend(bitmap: Bitmap, rotationDegrees: Int): List<Face> =
        suspendCancellableCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    continuation.resume(faces)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    fun close() {
        detector.close()
    }
}



