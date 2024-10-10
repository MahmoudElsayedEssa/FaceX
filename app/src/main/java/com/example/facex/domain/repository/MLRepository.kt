package com.example.facex.domain.repository

import com.example.facex.data.local.ml.entity.SimpleFace
import com.example.facex.domain.entities.ImageInput
import java.nio.ByteBuffer

interface MLRepository : AutoCloseable {
    suspend fun detectFaces(image: ImageInput, rotationDegrees: Int): List<SimpleFace>
    suspend fun generateEmbedding(face: ImageInput): ByteBuffer
}