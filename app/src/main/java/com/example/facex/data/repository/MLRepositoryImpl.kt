package com.example.facex.data.repository

import com.example.facex.data.local.ml.MLManager
import com.example.facex.data.local.ml.entity.SimpleFace
import com.example.facex.domain.entities.ImageInput
import com.example.facex.domain.repository.MLRepository
import java.nio.ByteBuffer
import javax.inject.Inject

class MLRepositoryImpl @Inject constructor(
    private val mlManager: MLManager
) : MLRepository {

    override suspend fun detectFaces(
        image: ImageInput,
        rotationDegrees: Int
    ): List<SimpleFace> {
        return mlManager.detectFaces(image, rotationDegrees).getOrThrow()
    }

    override suspend fun generateEmbedding(face: ImageInput): ByteBuffer {
        return mlManager.generateEmbedding(face).getOrThrow()
    }

    override fun close() {
        mlManager.close()
    }
}
