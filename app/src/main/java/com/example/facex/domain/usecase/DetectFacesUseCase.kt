package com.example.facex.domain.usecase

import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.toGrayScale
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.helpers.measureAndTrackPerformance
import com.example.facex.domain.repository.MLRepository
import com.example.facex.ui.FrameData
import com.example.facex.ui.utils.scale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DetectFacesUseCase @Inject constructor(
    private val mlRepository: MLRepository,
    private val performanceTracker: PerformanceTracker,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(faceData: FrameData): List<DetectedFace> =
        withContext(defaultDispatcher) {
            measureAndTrackPerformance(performanceTracker, PerformanceTracker.DETECTION_TIME) {
                val scaledBitmap = faceData.bitmap.scale(SCALE_FACTOR)
                mlRepository.detectFaces(scaledBitmap, faceData.rotationDegrees).map { face ->
                    coroutineScope {
                        async(defaultDispatcher) {
                            val croppedBitmap = measureAndTrackPerformance(
                                performanceTracker,
                                PerformanceTracker.FRAME_CROPPING_TIME
                            ) {
                                async {
                                    scaledBitmap.cropToBoundingBox(
                                        face.boundingBox,
                                        faceData.rotationDegrees
                                    )
                                }                            }
                            measureAndTrackPerformance(
                                performanceTracker,
                                PerformanceTracker.CONVERT_TO_GRAY_SCALE_TIME
                            ) {
                                croppedBitmap.await()?.toGrayScale()?.let { croppedBitmap ->
                                    DetectedFace(
                                        boundingBox = face.boundingBox.scale(1 / SCALE_FACTOR),
                                        trackedId = face.trackingId,
                                        bitmap = croppedBitmap
                                    )
                                }
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
            }

        }

    companion object {
        private const val SCALE_FACTOR = 0.25f
        private const val TAG = "DetectFacesUseCase"
    }

}
