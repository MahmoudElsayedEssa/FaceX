package com.example.facex.domain

import java.nio.ByteBuffer
import kotlin.math.sqrt


fun cosineSimilarity(embeddingA: ByteBuffer, embeddingB: ByteBuffer): Double {
    require(embeddingA.capacity() == embeddingB.capacity()) { "Buffers must have the same dimensions" }

    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0

    embeddingA.rewind()
    embeddingB.rewind()

    while (embeddingA.hasRemaining() && embeddingB.hasRemaining()) {
        val valueA = embeddingA.float
        val valueB = embeddingB.float

        dotProduct += valueA * valueB
        normA += valueA * valueA
        normB += valueB * valueB
    }

    embeddingA.rewind()
    embeddingB.rewind()

    val similarity = dotProduct / (sqrt(normA) * sqrt(normB))
    return similarity
}
