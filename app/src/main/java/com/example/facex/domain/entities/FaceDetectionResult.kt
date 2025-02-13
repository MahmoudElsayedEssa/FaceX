package com.example.facex.domain.entities

import android.graphics.Rect

data class FaceDetectionResult(
    val boundingBox: Rect,
    val trackingId: Int,
)
