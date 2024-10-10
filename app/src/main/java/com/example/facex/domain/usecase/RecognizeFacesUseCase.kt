package com.example.facex.domain.usecase

import android.graphics.ImageFormat
import android.util.Log
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.ImageInput
import com.example.facex.domain.entities.PerformanceTracker
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.helpers.FaceRecognizer
import com.example.facex.domain.repository.MLRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RecognizeFacesUseCase @Inject constructor(
    private val mlRepository: MLRepository,
    private val performanceTracker: PerformanceTracker,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        detectedFaces: List<DetectedFace?>,
        helper: FaceRecognizer
    ): List<RecognizedPerson> = withContext(defaultDispatcher) {
        if (detectedFaces.isEmpty()) {
            Log.d(TAG, "No faces detected")
            return@withContext emptyList()
        }

        Log.d(TAG, "Processing ${detectedFaces.size} detected faces")

        val recognizedPersons = performanceTracker.measureSuspendPerformance(
            PerformanceTracker.MetricKey.RECOGNITION_TIME
        ) {
            detectedFaces.mapNotNull { face ->
                processFace(face, helper)
            }
        }

        Log.d(TAG, "Recognition completed. Recognized ${recognizedPersons.size} persons")
        recognizedPersons
    }

    private suspend fun processFace(
        face: DetectedFace?,
        helper: FaceRecognizer
    ): RecognizedPerson? {
        if (face == null) {
            Log.d(TAG, "Skipping null face")
            return null
        }


        val embedding = getEmbedding(face)
        if (embedding == null) {
            Log.d(TAG, "Failed to get embedding for face")
            return null
        }

        return findRecognizedPerson(face, embedding, helper)
    }

    private suspend fun getEmbedding(face: DetectedFace): ByteBuffer? =
        performanceTracker.measureSuspendPerformance(PerformanceTracker.MetricKey.EMBEDDING_TIME) {
            try {
                face.imageByteBuffer.let { buffer ->
                    val inputImage = ImageInput.FromByteBuffer(
                        buffer,
                        face.boundingBox.width(),
                        face.boundingBox.height(),
                        ImageFormat.NV21
                    )
                    mlRepository.generateEmbedding(inputImage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting face embedding")
                null
            }
        }

    private suspend fun findRecognizedPerson(
        face: DetectedFace,
        embedding: ByteBuffer,
        helper: FaceRecognizer
    ): RecognizedPerson? =
        performanceTracker.measureSuspendPerformance(PerformanceTracker.MetricKey.FIND_RECOGNIZED_PERSON_TIME) {
            try {
                helper.findRecognizedPerson(face, embedding)
            } catch (e: Exception) {
                Log.e(TAG, "Error finding recognized person")
                null
            }
        }

    companion object {
        private const val TAG = "RecognizeFacesUseCase"
    }
}
