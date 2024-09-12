package com.example.facex.domain

import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import com.google.common.util.concurrent.AtomicDouble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.absoluteValue
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

fun cosineSimilarityParallel(
    vectorA: FloatArray,
    vectorB: FloatArray,
    normA: Double,
    normB: Double
): Double = vectorA.zip(vectorB).parallelStream().mapToDouble { (a, b) -> (a * b).toDouble() }
    .sum() / (normA * normB)


fun cosineSimilarityCombined(
    vectorA: FloatArray,
    vectorB: FloatArray,
    normA: Double,
    normB: Double,
    threshold: Float,
    chunkSize: Int = 1000
): Double {
    require(vectorA.size == vectorB.size) { "Vectors must have the same dimensions" }

    val dotProduct = AtomicDouble(0.0)
    val shouldStop = AtomicBoolean(false)

    vectorA.zip(vectorB)
        .chunked(chunkSize)
        .parallelStream()
        .forEach { chunk ->
            if (shouldStop.get()) {
                return@forEach
            }

            var chunkDotProduct = 0.0
            var chunkMaxPossible = 0.0

            for ((a, b) in chunk) {
                chunkDotProduct += a * b
                chunkMaxPossible += (a * b).absoluteValue
            }

            dotProduct.addAndGet(chunkDotProduct)

            val currentDotProduct = dotProduct.get()
            val remainingIndices = vectorA.size - chunk.last().first - 1
            val maxRemaining =
                remainingIndices * (vectorA.last().absoluteValue * vectorB.last().absoluteValue)

            if (currentDotProduct + maxRemaining < threshold * normA * normB) {
                shouldStop.set(true)
            }
        }

    return dotProduct.get() / (normA * normB)
}

//suspend fun List<Person>.findRecognizedPerson(
//    detectedFace: DetectedFace,
//    embedding: Embedding
//): RecognizedPerson? {
//    val normA = sqrt(embedding.sumOf { it.toDouble().pow(2.0) })
//    return withContext(Dispatchers.Default) {
//        this@findRecognizedPerson.chunked(10).map { chunk ->
//            async {
//                chunk.mapNotNull { person ->
//                    val similarity = cosineSimilarityParallel(embedding, person.embedding, normA, person.norm)
//                    if (similarity >= RECOGNITION_CONFIDENCE_THRESHOLD) {
//                        RecognizedPerson(person, similarity, detectedFace)
//                    } else {
//                        null
//                    }
//                }
//            }
//        }.awaitAll().flatten().maxByOrNull { it.confidence }
//    }
//}


suspend fun List<Person>.findRecognizedPerson(
    detectedFace: DetectedFace,
    embedding: Embedding
): RecognizedPerson? {
    val normA = sqrt(embedding.sumOf { it.toDouble().pow(2.0) })
    val threshold = RECOGNITION_CONFIDENCE_THRESHOLD
    val bestMatch = AtomicReference<RecognizedPerson?>(null)

    return withContext(Dispatchers.Default) {
        val jobs = this@findRecognizedPerson.chunked(100).map { chunk ->
            launch {
                var localBestMatch: RecognizedPerson? = null
                for (person in chunk) {
                    val similarity = cosineSimilarityCombined(
                        embedding,
                        person.embedding,
                        normA,
                        person.norm,
                        threshold
                    )
                    if (similarity >= threshold) {
                        val recognizedPerson = RecognizedPerson(person, similarity, detectedFace)
                        if (localBestMatch == null || similarity > localBestMatch.confidence) {
                            localBestMatch = recognizedPerson
                        }
                    }
                }
                localBestMatch?.let { newMatch ->
                    while (true) {
                        val currentBest = bestMatch.get()
                        if (currentBest == null || newMatch.confidence > currentBest.confidence) {
                            if (bestMatch.compareAndSet(currentBest, newMatch)) break
                        } else {
                            break
                        }
                    }
                }
            }
        }
        jobs.joinAll()
        bestMatch.get()
    }
}


