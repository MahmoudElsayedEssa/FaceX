package com.example.facex.domain.repository

import android.graphics.Bitmap
import com.example.facex.domain.entities.DetectedFace

interface MLRepository {
    fun detectFacesInImage(
        bitmap: Bitmap,
        rotationDegrees: Int,
        onFacesDetected: (detectedFaces: List<DetectedFace>) -> Unit

    )
}