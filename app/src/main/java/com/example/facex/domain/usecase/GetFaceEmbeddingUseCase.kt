package com.example.facex.domain.usecase

import android.graphics.ImageFormat
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.ImageInput
import com.example.facex.domain.repository.MLRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFaceEmbeddingUseCase @Inject constructor(
    private val mlRepository: MLRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(faceByteBuffer: ByteBuffer, width: Int, height: Int): Embedding =
        withContext(defaultDispatcher) {
            val imageInput =
                ImageInput.FromByteBuffer(faceByteBuffer, width, height, ImageFormat.NV21)
            mlRepository.generateEmbedding(imageInput)
        }
}
