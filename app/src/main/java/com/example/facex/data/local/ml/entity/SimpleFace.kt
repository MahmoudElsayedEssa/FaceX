package com.example.facex.data.local.ml.entity

import android.graphics.Rect

data class SimpleFace(
    val boundingBox: Rect,
    val trackedId: Int
)
