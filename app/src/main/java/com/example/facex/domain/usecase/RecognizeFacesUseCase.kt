package com.example.facex.domain.usecase

import com.example.facex.di.DefaultDispatcher
import com.example.facex.di.IoDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.helpers.CustomANNFaceRecognizerLSH
import com.example.facex.domain.helpers.measureAndTrackPerformance
import com.example.facex.domain.repository.MLRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RecognizeFacesUseCase @Inject constructor(
    private val mlRepository: MLRepository,
    private val performanceTracker: PerformanceTracker,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        detectedFaces: List<DetectedFace?>, recognizerLSH: CustomANNFaceRecognizerLSH
    ): List<RecognizedPerson> = withContext(defaultDispatcher) {
        if (detectedFaces.isEmpty()) return@withContext emptyList()

        val recognizedPersons = measureAndTrackPerformance(
            performanceTracker,
            PerformanceTracker.RECOGNITION_TIME
        ) {
            val jobs = detectedFaces.map { face ->
                async {
                    val embedding = measureAndTrackPerformance(
                        performanceTracker,
                        PerformanceTracker.EMBEDDING_TIME
                    ) {
                        async { face?.bitmap?.let { mlRepository.getFaceEmbedding(it) } }
                    }

                    val recognizedPerson = measureAndTrackPerformance(
                        performanceTracker,
                        PerformanceTracker.FIND_RECOGNIZED_PERSON_TIME
                    ) {
                        async{embedding.let {
                            face?.let { it1 ->
                                it.await()
                                    ?.let { it2 -> recognizerLSH.findRecognizedPerson(it1, it2) }
                            }
                        }}
                    }

                    recognizedPerson.await()
                }
            }

            Collections.synchronizedList(mutableListOf<RecognizedPerson>()).apply {
                addAll(jobs.awaitAll().filterNotNull())
            }
        }

        recognizedPersons
    }

    companion object {
        const val TAG = "RecognizeFacesUseCase"
    }
}

