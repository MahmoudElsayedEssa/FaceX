package com.example.facex.ui

import android.graphics.Bitmap

data class FrameData(
    val id: Long,
    val bitmap: Bitmap,
    val rotationDegrees: Int
)