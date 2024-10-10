package com.example.facex.data.local.ml.detection

import android.graphics.Bitmap
import com.example.facex.data.local.ml.entity.SimpleFace
import java.nio.ByteBuffer


interface FaceDetector : AutoCloseable {
    suspend fun detectFaces(bitmap: Bitmap, rotationDegrees: Int = 0): Result<List<SimpleFace>>

    suspend fun detectFaces(
        buffer: ByteBuffer, width: Int, height: Int, format: Int, rotationDegrees: Int
    ): Result<List<SimpleFace>>

    suspend fun detectFaces(
        array: ByteArray, width: Int, height: Int, format: Int, rotationDegrees: Int
    ): Result<List<SimpleFace>>
}