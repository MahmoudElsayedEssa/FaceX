package com.cheesecake.platex.data.repository

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.data.local.ml.facerecognition.FaceRecognizer
import com.example.facex.domain.repository.MLRepository
import java.nio.ByteBuffer

import javax.inject.Inject

class MLRepositoryImpl @Inject constructor(
    private val faceRecognizer: FaceRecognizer
) : MLRepository {
    override fun detectFacesInImage(
        bitmap: Bitmap,
        onFaceDetected: (embedding: ByteBuffer, boundingBox: Rect) -> Unit
    ) {
        faceRecognizer.detectFacesInImage(bitmap, onFaceDetected)
    }

    override fun recognizeFaces(bitmap: Bitmap, onRecognizeFace: (embedding: ByteBuffer) -> Unit) {
        faceRecognizer.recognizeFaces(bitmap, onRecognizeFace)
    }

}