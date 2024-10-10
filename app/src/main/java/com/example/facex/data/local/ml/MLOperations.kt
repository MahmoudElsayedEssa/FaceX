package com.example.facex.data.local.ml

import com.example.facex.data.local.ml.entity.SimpleFace
import com.example.facex.domain.entities.ImageInput
import java.nio.ByteBuffer

interface MLOperations {
    suspend fun detectFaces(image: ImageInput, rotationDegrees: Int): Result<List<SimpleFace>>
    suspend fun generateEmbedding(face: ImageInput): Result<ByteBuffer>
}