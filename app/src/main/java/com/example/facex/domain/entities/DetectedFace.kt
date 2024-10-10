package com.example.facex.domain.entities

import android.graphics.Rect
import java.nio.ByteBuffer

data class DetectedFace(
    val boundingBox: Rect,
    val trackedId: Int?,
    val imageByteBuffer: ByteBuffer,
)