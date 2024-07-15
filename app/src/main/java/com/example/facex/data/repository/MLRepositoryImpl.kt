package com.example.facex.data.repository

import android.graphics.Bitmap
import com.example.facex.data.local.ml.MyFaceDetector
import com.example.facex.data.local.ml.facerecognition.MyFaceRecognizer
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.repository.MLRepository
import javax.inject.Inject

class MLRepositoryImpl @Inject constructor(
    private val faceRecognizer: MyFaceRecognizer,
    private val faceDetector: MyFaceDetector,
) : MLRepository {
    override suspend fun recognizeFaces(bitmap: Bitmap, rotationDegrees: Int)
            : List<DetectedFace> {
        val faces = faceDetector.detectFaces(bitmap, rotationDegrees)
        return faceRecognizer.recognizeFaces(bitmap, rotationDegrees, faces)
    }

    override fun stopRecognition() {
        faceRecognizer.close()
        faceDetector.close()
    }


}