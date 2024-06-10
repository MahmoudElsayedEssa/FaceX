package com.example.facex.domain.usecase

import android.graphics.Bitmap
import com.example.facex.data.local.ml.facerecognition.FaceRecognizer
import java.nio.ByteBuffer
import javax.inject.Inject

class CalculateEmbeddingUseCase @Inject constructor(
    private val faceRecognizer: FaceRecognizer
) {
    suspend operator fun invoke(faceBitmap: Bitmap): ByteBuffer {
        return faceRecognizer.calculateEmbedding(faceBitmap)
    }


}
