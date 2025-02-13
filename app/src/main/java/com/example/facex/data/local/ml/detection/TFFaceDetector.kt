package com.example.facex.data.local.ml.detection

import com.example.facex.domain.entities.FaceDetectionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.ml.FaceDetector

class TFFaceDetector(
) : FaceDetector {
    override suspend fun detectFaces(image: Frame): Result<List<FaceDetectionResult>> {
        TODO("Not yet implemented")
    }

    override suspend fun close() {
        TODO("Not yet implemented")
    }

}
