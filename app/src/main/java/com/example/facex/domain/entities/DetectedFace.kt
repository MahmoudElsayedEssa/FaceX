package com.example.facex.domain.entities

import android.graphics.Bitmap
import android.graphics.Rect

data class DetectedFace(
    val boundingBox: Rect,
    val trackedId: Int?,
    val bitmap: Bitmap,
    val embedding: Embedding
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectedFace

        if (boundingBox != other.boundingBox) return false
        if (trackedId != other.trackedId) return false
        if (bitmap != other.bitmap) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = boundingBox.hashCode()
        result = 31 * result + (trackedId ?: 0)
        result = 31 * result + bitmap.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}