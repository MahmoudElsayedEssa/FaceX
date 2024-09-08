package com.example.facex.domain.entities

import kotlin.math.pow
import kotlin.math.sqrt

data class Person(
    val id: Long,
    val name: String,
    val embedding: Embedding,
    val norm: Double = sqrt(embedding.sumOf { it.toDouble().pow(2.0) })

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (id != other.id) return false
        if (name != other.name) return false
        if (!embedding.contentEquals(other.embedding)) return false
        if (norm != other.norm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + embedding.contentHashCode()
        result = 31 * result + norm.hashCode()
        return result
    }
}