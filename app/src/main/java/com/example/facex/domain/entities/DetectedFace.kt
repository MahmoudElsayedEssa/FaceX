package com.example.facex.domain.entities

import android.graphics.Rect

data class DetectedFace(
    val faceImage: Frame,
    val boundingBox: Rect,
    val trackingId: Int,
)
