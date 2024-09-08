package com.example.facex.data.local.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import javax.inject.Inject
import javax.inject.Singleton

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
            .addOnFailureListener { exception ->
            }
    }

    fun close() {
        detector.close()
    }
}

/*
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    fun detectFacesContinuously(imageFlow: Flow<Pair<Bitmap, Int>>): Flow<List<Face>> = flow {
        imageFlow.collect { (bitmap, rotationDegrees) ->
            val faces = detectFaces(bitmap, rotationDegrees)
            emit(faces)
        }
    }
    suspend fun detectFaces(bitmap: Bitmap, rotationDegrees: Int): List<Face> =
        suspendCancellableCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    continuation.resume(faces)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }

            continuation.invokeOnCancellation {
                // If the coroutine is cancelled, we should stop the detection process
                // However, ML Kit's FaceDetector doesn't provide a way to cancel ongoing operations
                // So we'll just log it for now
                Log.w("MyFaceDetector", "Face detection was cancelled")
            }
        }

    fun close() {
        detector.close()
    }
}*/