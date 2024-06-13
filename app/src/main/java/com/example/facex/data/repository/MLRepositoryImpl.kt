package com.example.facex.data.repository

import android.graphics.Bitmap
import com.example.facex.data.local.ml.facerecognition.FaceRecognizer
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.repository.MLRepository
import javax.inject.Inject

class MLRepositoryImpl @Inject constructor(
    private val faceRecognizer: FaceRecognizer
) : MLRepository {
    override fun detectFacesInImage(
        bitmap: Bitmap,
        rotationDegrees: Int,
        onFacesDetected: (detectedFaces: List<DetectedFace>) -> Unit
    ) {
        faceRecognizer.detectFacesInImage(bitmap, rotationDegrees, onFacesDetected)
    }


}