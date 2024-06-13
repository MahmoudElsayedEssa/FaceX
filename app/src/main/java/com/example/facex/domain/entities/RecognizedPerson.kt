package com.example.facex.domain.entities

import android.graphics.Rect
import java.nio.ByteBuffer

data class RecognizedPerson(
    val person: Person,
    val confidence: Double
)


data class DetectedFace(
    val boundingBox: Rect,
    val trackedId: Int?,
    val embedding: ByteBuffer?
)