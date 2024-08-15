package com.example.facex.domain.usecase

import android.graphics.Bitmap
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.repository.MLRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFaceEmbeddingUseCase @Inject constructor(
    private val mlRepository: MLRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(faceBitmap: Bitmap): Embedding = withContext(defaultDispatcher) {
        mlRepository.getFaceEmbedding(faceBitmap)
    }
}
