package com.example.facex.domain.repository

import android.graphics.Bitmap
import com.example.facex.domain.entities.Embedding
import com.google.mlkit.vision.face.Face

interface MLRepository : FaceDetector, FaceRecognizer {
    fun stopRecognition()
}

interface FaceDetector {
    suspend   fun detectFaces(
        bitmap: Bitmap,
        rotationDegrees: Int,
        callback: (List<Face>) -> Unit
    )
    suspend fun detectFaces(
        bitmap: Bitmap, rotationDegrees: Int
    ): List<Face>

}

interface FaceRecognizer {
    suspend fun getFaceEmbedding(faceBitmap: Bitmap): Embedding
}