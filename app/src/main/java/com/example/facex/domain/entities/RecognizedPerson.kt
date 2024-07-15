package com.example.facex.domain.entities

import android.graphics.Bitmap
import android.graphics.Rect
import java.nio.ByteBuffer

data class RecognizedPerson(
    val person: Person,
    val confidence: Double,
    val detectedFace: DetectedFace
)


