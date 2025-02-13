package com.example.facex.domain.ml

import com.example.facex.domain.entities.Frame

interface EmbeddingGenerator : MlOperation {
    suspend fun generateEmbedding(
        input: Frame,
    ): Result<FloatArray>

}
