package com.example.facex.ui

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.domain.entities.RecognizedPerson

data class TrackedFace(
    val id: Int,
    val boundingBox: Rect,
    val bitmap: Bitmap,
    val recognizedPerson: RecognizedPerson? = null
) {
    val isRecognized: Boolean
        get() = recognizedPerson != null

    val displayName: String
        get() = recognizedPerson?.person?.name ?: "Unknown"

    val confidence: Double
        get() = recognizedPerson?.confidence ?: 0.0
}
