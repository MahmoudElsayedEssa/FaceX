package com.example.facex.domain.repository

import android.graphics.Bitmap
import android.graphics.Rect
import java.nio.ByteBuffer

interface MLRepository {
    fun detectFacesInImage(
        bitmap: Bitmap,
        onFaceDetected: (embedding: ByteBuffer, boundingBox: Rect) -> Unit
    )

    fun recognizeFaces(bitmap: Bitmap, onRecognizeFace: (embedding: ByteBuffer) -> Unit)


}