package com.example.facex.data.local.ml.embeddings_generator

import com.example.facex.domain.entities.ImageInput
import java.nio.ByteBuffer

interface EmbeddingGenerator : AutoCloseable {
    suspend fun generateEmbedding(input: ImageInput): Result<ByteBuffer>
    fun getEmbeddingAsFloatArray(): FloatArray
}
