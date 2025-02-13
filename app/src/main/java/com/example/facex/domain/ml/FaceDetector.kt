package com.example.facex.domain.ml

import com.example.facex.domain.entities.FaceDetectionResult
import com.example.facex.domain.entities.Frame

interface FaceDetector :  MlOperation {
    suspend fun detectFaces(image: Frame): Result<List<FaceDetectionResult>>
}
