package com.example.facex.domain.entities

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import kotlin.math.pow
import kotlin.math.sqrt

data class Person(
    val id: Long,
    val name: String,
    val embedding: Embedding,
    val norm: Float = calculateNorm(embedding)
) {
    companion object {
        fun calculateNorm(embedding: ByteBuffer): Float {
            val floatBuffer: FloatBuffer = embedding.asFloatBuffer()
            var sum = 0.0F

            for (i in 0 until floatBuffer.limit()) {
                val value = floatBuffer.get(i)
                sum += value * value
            }
            return sqrt(sum)
        }
    }}
