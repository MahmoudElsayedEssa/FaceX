package com.example.facex.domain.usecase

import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.performancetracking.PerformanceTracker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class FaceMatchingConfig(
    val recognitionThreshold: Float = 0.70f
)

@Singleton
class FindBestMatchUseCase @Inject constructor(
    private val performanceTracker: PerformanceTracker,
    private val logger: Logger,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    private var matchingConfig = FaceMatchingConfig()

    init {
        logger.tag = this::class.simpleName.toString()
    }

    fun updateConfig(threshold: Float) {
        matchingConfig = matchingConfig.copy(recognitionThreshold = threshold)
    }

    suspend operator fun invoke(
        faceEmbedding: Embedding, knownPersons: List<Person>
    ): Pair<Person, Float>? = knownPersons.takeUnless { it.isEmpty() }?.let { persons ->
        withContext(defaultDispatcher) {
            val chunks = persons.chunked(calculateChunkSize(persons.size))
            chunks.map { chunk ->
                async {
                    findBestMatchInChunk(chunk, faceEmbedding)
                }
            }.awaitAll().filterNotNull().maxByOrNull { it.second }
        }
    }

    private fun calculateChunkSize(size: Int): Int {
        val parallelChunks = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        return (size / parallelChunks).coerceAtLeast(MIN_CHUNK_SIZE).coerceAtMost(MAX_CHUNK_SIZE)
    }

    private fun findBestMatchInChunk(
        chunk: List<Person>, faceEmbedding: Embedding
    ): Pair<Person, Float>? {
        var bestMatch: Person? = null
        var bestSimilarity = matchingConfig.recognitionThreshold


        // Avoid allocations in hot loop
        for (person in chunk) {
            val similarity = faceEmbedding.rawCosineSimilarity(person.embedding)
            "Similarity with ${person.name}: $similarity, bestSimilarity: $bestSimilarity".logDebug(
                logger
            )

            if (similarity > bestSimilarity) {
                "New best match: ${person.name} ($similarity)".logDebug(logger)
                bestMatch = person
                bestSimilarity = similarity
            }
        }

        return bestMatch?.let { it to bestSimilarity }
    }

    companion object {
        private const val MIN_CHUNK_SIZE = 10
        private const val MAX_CHUNK_SIZE = 100

    }
}