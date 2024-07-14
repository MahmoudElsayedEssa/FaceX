package com.example.facex.domain.repository

import android.graphics.Bitmap
import com.example.facex.domain.entities.DetectedFace
import kotlinx.coroutines.flow.Flow

interface MLRepository {
    fun recognizeFaces(bitmap: Bitmap, rotationDegrees: Int): Flow<List<DetectedFace>>

    fun stopRecognition()
}