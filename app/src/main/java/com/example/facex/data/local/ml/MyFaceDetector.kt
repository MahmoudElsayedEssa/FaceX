package com.example.facex.data.local.ml

import android.graphics.Bitmap
import com.example.facex.di.DefaultDispatcher
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class MyFaceDetector @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val detector: com.google.mlkit.vision.face.FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(2f)
                .enableTracking()
                .build()
        )
    }

    suspend fun detectFaces(bitmap: Bitmap, rotationDegrees: Int): List<Face> =
        withContext(defaultDispatcher) {
            suspendCoroutine { continuation ->
                val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
                detector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        continuation.resume(faces)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
        }

    fun close() {
        detector.close()
    }
}
