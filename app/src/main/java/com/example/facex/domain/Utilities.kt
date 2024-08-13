package com.example.facex.domain

import android.media.FaceDetector.Face.CONFIDENCE_THRESHOLD
import android.util.Log
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.math.pow
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

    return dotProduct / (sqrt(normA) * sqrt(normB))
}

fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Double {
    require(vectorA.size == vectorB.size) { "Vectors must have the same dimensions" }

    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0
    for (i in vectorA.indices) {
        dotProduct += vectorA[i] * vectorB[i]
        normA += vectorA[i].toDouble().pow(2.0)
        normB += vectorB[i].toDouble().pow(2.0)
    }
    return dotProduct / (sqrt(normA) * sqrt(normB))
}

suspend fun List<Person>.findRecognizedPerson(detectedFace: DetectedFace,embedding:Embedding): RecognizedPerson? =
    withContext(
        Dispatchers.Default
    ) {
         this@findRecognizedPerson.mapNotNull { person ->
             embedding.let {
                 cosineSimilarity(it, person.embedding).takeIf {
                     it >= CONFIDENCE_THRESHOLD
                 }?.let { confidence ->
                     RecognizedPerson(person, confidence, detectedFace).also {
                         Log.d("findRecognizedPerson", "findRecognizedPerson: confidence: $confidence")
                     }
                 }
             }
        }.maxByOrNull { it.confidence }
    }

inline fun logExecutionTime(tag: String, description: String, block: () -> Unit) {
    val startTime = System.nanoTime()
    block()
    val endTime = System.nanoTime()
    val duration = endTime - startTime

    val message = when {
        duration < 1_000_000 -> "$description took ${duration / 1000} µs"
        duration < 1_000_000_000 -> "$description took ${duration / 1_000_000} ms"
        else -> "$description took ${duration / 1_000_000_000} s"
    }
    Log.d(tag, "logExecutionTime: $message")
}

