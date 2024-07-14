package com.example.facex.data.repository

import android.graphics.Bitmap
import com.example.facex.data.local.ml.facerecognition.FaceRecognizer
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.repository.MLRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MLRepositoryImpl @Inject constructor(
    private val faceRecognizer: FaceRecognizer
) : MLRepository {
    override fun recognizeFaces(bitmap: Bitmap, rotationDegrees: Int)
            : Flow<List<DetectedFace>> = faceRecognizer.detectFacesInImage(bitmap, rotationDegrees)

    override fun stopRecognition() {
        faceRecognizer.stop()
    }


}