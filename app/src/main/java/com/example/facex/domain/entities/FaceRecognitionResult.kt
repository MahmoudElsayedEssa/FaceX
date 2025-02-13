package com.example.facex.domain.entities

data class FaceRecognitionResult(
    val faceDetection: DetectedFace,
    val recognitionStatus: RecognitionStatus,
)
