package com.example.facex.domain.repository

import android.graphics.Bitmap
import com.example.facex.domain.entities.DetectedFace

interface MLRepository {
    suspend fun recognizeFaces(bitmap: Bitmap, rotationDegrees: Int): List<DetectedFace>

    fun stopRecognition()
}