package com.example.facex.domain.usecase

import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.ImageInput
import com.example.facex.domain.entities.PerformanceTracker
import com.example.facex.domain.repository.MLRepository
import com.example.facex.ui.FrameData
import com.example.facex.ui.helpers.operations.CropOperator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DetectFacesUseCase @Inject constructor(
    private val mlRepository: MLRepository,
    private val performanceTracker: PerformanceTracker,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(frameData: FrameData): List<DetectedFace> =
        withContext(defaultDispatcher) {
            performanceTracker.measureSuspendPerformance(PerformanceTracker.MetricKey.DETECTION_TIME) {
                val detectedFaces =
                    mlRepository.detectFaces(frameData.imageInput, frameData.rotationDegrees)

                detectedFaces.map { face ->
                    async {
                        val cropOperator = CropOperator(face.boundingBox)
                        when (val croppedImage = cropOperator.process(frameData.imageInput)) {
                            is ImageInput.FromByteBuffer -> DetectedFace(
                                boundingBox = face.boundingBox,
                                trackedId = face.trackedId,
                                imageByteBuffer = croppedImage.buffer
                            )

                            else -> null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        }


    companion object {
        private const val TAG = "DetectFacesUseCase"
    }
}


