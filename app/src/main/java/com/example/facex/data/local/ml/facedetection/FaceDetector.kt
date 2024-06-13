package com.example.facex.data.local.ml.facedetection

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import javax.inject.Inject

class FaceDetector @Inject constructor() {
    private val detector: FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .enableTracking()
                .build()
        )
    }


    fun detectFacesInImage(bitmap: Bitmap, rotationDegrees: Int): Task<List<Face>> {
        val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
        return detector.process(inputImage)
    }

    fun stop() {
        detector.close()
    }
}
