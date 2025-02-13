package com.example.facex.domain.usecase

import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.performancetracking.PerformanceTracker
import com.example.facex.domain.performancetracking.PerformanceTrackingKeys.EMBEDDING_GENERATION_TIME
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFaceEmbeddingUseCase
@Inject constructor(
    private val faceProcessor: FaceProcessorFacade,
    private val performanceTracker: PerformanceTracker,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(faceImageData: Frame): Result<Embedding> =
        withContext(defaultDispatcher) {
            performanceTracker.suspendTrack(EMBEDDING_GENERATION_TIME) {
                faceProcessor.generateEmbedding(faceImageData).map {
                    it.toEmbedding()
                }
            }
        }
}

private fun FloatArray.toEmbedding() = Embedding(data = this)
