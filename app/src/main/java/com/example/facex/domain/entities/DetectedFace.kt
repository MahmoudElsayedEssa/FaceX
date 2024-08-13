package com.example.facex.domain.entities

import android.graphics.Bitmap
import android.graphics.Rect

data class DetectedFace(
    val boundingBox: Rect,
    val trackedId: Int?,
    val bitmap: Bitmap,
)