package com.example.facex.domain.entities

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Embedding(val data: FloatArray) {

    fun rawCosineSimilarity(other: Embedding): Float {
        val dotProduct = data.zip(other.data) { a, b -> a * b }.sum()
        val mag1 = sqrt(data.sumOf { it * it.toDouble() })
        val mag2 = sqrt(other.data.sumOf { it * it.toDouble() })

        return ((dotProduct / (mag1 * mag2)).coerceIn(-1.0, 1.0) + 1.0).toFloat() / 2f
    }
}


