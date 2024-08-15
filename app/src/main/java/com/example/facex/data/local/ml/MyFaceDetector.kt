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
                .setMinFaceSize(2f)
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
            .addOnFailureListener { exception ->
            }
    }

    fun close() {
        detector.close()
    }
}
