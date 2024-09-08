package com.example.facex.domain

import android.util.Log
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt

const val RECOGNITION_CONFIDENCE_THRESHOLD = 0.6f
fun cosineSimilarity(
    vectorA: FloatArray,
    vectorB: FloatArray,
    normA: Double,
    normB: Double
): Double {
    require(vectorA.size == vectorB.size) { "Vectors must have the same dimensions" }

    var dotProduct = 0.0
    for (i in vectorA.indices) {
        dotProduct += vectorA[i] * vectorB[i]
    }
    return dotProduct / (normA * normB)
}


suspend fun List<Person>.findRecognizedPerson(
    detectedFace: DetectedFace,
    embedding: Embedding
): RecognizedPerson? {
    val normA = sqrt(embedding.sumOf { it.toDouble().pow(2.0) })

    return withContext(Dispatchers.Default) {
        mapNotNull { person ->
            async {
                cosineSimilarity(embedding, person.embedding, normA, person.norm).takeIf {
                    it >= RECOGNITION_CONFIDENCE_THRESHOLD
                }?.let { confidence ->
                    RecognizedPerson(person, confidence, detectedFace).also {
                        Log.d("findRecognizedPersonParallel", "Confidence: $confidence")
                    }
                }
            }
        }.awaitAll().filterNotNull().maxByOrNull { it.confidence }
    }
}



