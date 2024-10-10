package com.example.facex.ui

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.domain.entities.RecognizedPerson
import java.nio.ByteBuffer

data class TrackedFace(
    val id: Int,
    val boundingBox: Rect,
    val imageByteBuffer: ByteBuffer,
    val recognizedPerson: RecognizedPerson? = null
) {
    val isRecognized: Boolean
        get() = recognizedPerson != null

    val displayName: String
        get() = recognizedPerson?.person?.name ?: "Unknown"

    val confidence: Float
        get() = recognizedPerson?.confidence ?: 0.0F
}
