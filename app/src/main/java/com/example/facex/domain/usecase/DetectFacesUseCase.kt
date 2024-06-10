package com.example.facex.domain.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.data.local.ml.facerecognition.FaceRecognizer
import java.nio.ByteBuffer
import javax.inject.Inject

class DetectFacesUseCase @Inject constructor(
    private val faceRecognizer: FaceRecognizer
) {
    operator fun invoke(
        bitmap: Bitmap,
        onFaceDetected: (embedding: ByteBuffer, boundingBox: Rect) -> Unit
    ) {
        faceRecognizer.detectFacesInImage(bitmap, onFaceDetected)
    }


}
