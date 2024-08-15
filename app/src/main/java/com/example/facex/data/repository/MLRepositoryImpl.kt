package com.example.facex.data.repository

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.data.local.ml.MyFaceDetector
import com.example.facex.data.local.ml.facerecognition.MyFaceRecognizer
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.repository.MLRepository
import com.google.mlkit.vision.face.Face
import javax.inject.Inject

class MLRepositoryImpl @Inject constructor(
    private val faceRecognizer: MyFaceRecognizer,
    private val faceDetector: MyFaceDetector,
) : MLRepository {
    override fun detectFaces(
        bitmap: Bitmap,
        rotationDegrees: Int,
        callback: (List<Face>) -> Unit
    ) {
        faceDetector.detectFaces(bitmap, rotationDegrees, callback)
    }


    override suspend fun getFaceEmbedding(faceBitmap: Bitmap): Embedding {
        return faceRecognizer.calculateEmbeddingFloatArray(faceBitmap)
    }

    override fun stopRecognition() {
        faceRecognizer.close()
        faceDetector.close()
    }
}