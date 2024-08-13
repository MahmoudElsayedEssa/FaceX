package com.example.facex.domain.repository

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.google.mlkit.vision.face.Face

interface MLRepository:FaceDetector, FaceRecognizer{
    fun stopRecognition()
}
interface FaceDetector {
    fun detectFaces(
        bitmap: Bitmap,
        rotationDegrees: Int,
        callback: (List<Face>) -> Unit
    )
}

interface FaceRecognizer {
    suspend fun getFaceEmbedding(faceBitmap: Bitmap, boundingBox: Rect): Embedding
}