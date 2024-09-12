package com.example.facex.domain.helpers

import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class CustomANNFaceRecognizerLSH(private val knownFaces: List<Person>) {
    companion object {
        private const val ORIGINAL_DIMENSION = 512
        private const val REDUCED_DIMENSION = 64
        private const val NUM_HASH_TABLES = 10
        private const val HASH_SIZE = 16
        private const val CHUNK_SIZE = 1000
        private const val RECOGNITION_CONFIDENCE_THRESHOLD = 0.6f
    }

    private val projectionMatrix = generateRandomProjectionMatrix(ORIGINAL_DIMENSION, REDUCED_DIMENSION)
    private val randomHyperplanes = generateRandomHyperplanes()
    private val hashTables = buildHashTables()

    private fun generateRandomProjectionMatrix(fromDim: Int, toDim: Int) =
        Array(toDim) { FloatArray(fromDim) { Random.nextFloat() * 2 - 1 } }

    private fun generateRandomHyperplanes() =
        List(NUM_HASH_TABLES) { Array(HASH_SIZE) { FloatArray(REDUCED_DIMENSION) { Random.nextFloat() * 2 - 1 } } }

    private fun buildHashTables(): List<MutableMap<String, MutableList<Person>>> {
        val tables = List(NUM_HASH_TABLES) { mutableMapOf<String, MutableList<Person>>() }
        knownFaces.forEach { person ->
            val projectedEmbedding = projectVector(person.embedding)
            for (tableIndex in 0 until NUM_HASH_TABLES) {
                val hashCode = hashVector(projectedEmbedding, randomHyperplanes[tableIndex])
                tables[tableIndex].getOrPut(hashCode) { mutableListOf() }.add(person)
            }
        }
        return tables
    }

    private fun projectVector(vector: FloatArray): FloatArray =
        FloatArray(REDUCED_DIMENSION) { i ->
            vector.mapIndexed { j, v -> v * projectionMatrix[i][j] }.sum()
        }

    private fun hashVector(vector: FloatArray, randomHyperplane: Array<FloatArray>): String =
        randomHyperplane.joinToString("") { if (vector.dotProduct(it) >= 0) "1" else "0" }

    private fun getCandidateMatches(projectedQuery: FloatArray): Set<Person> =
        hashTables.flatMapTo(mutableSetOf()) { table ->
            val hashCode = hashVector(projectedQuery, randomHyperplanes[hashTables.indexOf(table)])
            table[hashCode] ?: emptyList()
        }

    suspend fun findRecognizedPerson(
        detectedFace: DetectedFace,
        embedding: FloatArray
    ): RecognizedPerson? = withContext(Dispatchers.Default) {
        val projectedQuery = projectVector(embedding)
        val normA = embedding.norm()
        val candidates = getCandidateMatches(projectedQuery)

        candidates.asSequence()
            .map { person ->
                async {
                    val similarity = cosineSimilarity(embedding, person.embedding, normA, person.norm)
                    if (similarity >= RECOGNITION_CONFIDENCE_THRESHOLD) {
                        RecognizedPerson(person, similarity, detectedFace)
                    } else null
                }
            }
            .toList()
            .awaitAll()
            .filterNotNull()
            .maxByOrNull { it.confidence }
    }

    private fun cosineSimilarity(
        vectorA: FloatArray,
        vectorB: FloatArray,
        normA: Double,
        normB: Double
    ): Double {
        require(vectorA.size == vectorB.size) { "Vectors must have the same dimensions" }

        var dotProduct = 0.0
        var i = 0
        while (i < vectorA.size) {
            val chunkEnd = (i + CHUNK_SIZE).coerceAtMost(vectorA.size)
            var chunkDotProduct = 0.0
            for (j in i until chunkEnd) {
                chunkDotProduct += vectorA[j] * vectorB[j]
            }
            dotProduct += chunkDotProduct

            val remainingIndices = vectorA.size - chunkEnd
            val maxRemaining = remainingIndices * (vectorA.last().absoluteValue * vectorB.last().absoluteValue)
            if (dotProduct + maxRemaining < RECOGNITION_CONFIDENCE_THRESHOLD * normA * normB) {
                return 0.0
            }
            i = chunkEnd
        }
        return dotProduct / (normA * normB)
    }

    private fun FloatArray.dotProduct(other: FloatArray): Float =
        zip(other).sumOf { (a, b) -> a * b.toDouble() }.toFloat()

    private fun FloatArray.norm(): Double =
        sqrt(sumOf { it.toDouble().pow(2) })
}
