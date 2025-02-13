package com.example.facex.domain.ml

import com.example.facex.domain.entities.FaceDetectionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.ml.config.FaceDetectionService
import com.example.facex.domain.ml.config.FaceRecognitionService

interface MlOperation {
    suspend fun close()
}

interface FaceProcessorFacade {
    suspend fun detectFaces(
        image: Frame,
    ): Result<List<FaceDetectionResult>>

    suspend fun generateEmbedding(
        input: Frame,
    ): Result<FloatArray>

    suspend fun updateFaceDetector(newDetector: FaceDetector): Result<Unit>

    suspend fun updateFaceRecognition(newDetector: EmbeddingGenerator): Result<Unit>

    suspend fun getCurrentDetectorType(): Result<FaceDetectionService>
    suspend fun getCurrentRecognitionType(): Result<FaceRecognitionService>

    suspend fun close()
}
