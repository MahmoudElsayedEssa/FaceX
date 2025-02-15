package com.example.facex.domain.usecase

import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.performancetracking.PerformanceTracker
import com.example.facex.domain.performancetracking.PerformanceTrackingKeys.DETECTION_TIME
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectFacesUseCase @Inject constructor(
    private val faceProcessor: FaceProcessorFacade,
    private val performanceTracker: PerformanceTracker,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        frame: Frame,
    ): Result<List<DetectedFace>> = withContext(defaultDispatcher) {
        performanceTracker.suspendTrack(DETECTION_TIME) {
            faceProcessor.detectFaces(frame).map { faces ->
                faces.map { face ->
                    async {
                        val croppedFace = frame
                            .toGrayscale()
                            .alignRotation()
                            .crop(face.boundingBox)
                        DetectedFace(croppedFace, face.boundingBox, face.trackingId)
                    }
                }.awaitAll()
            }
        }
    }
}

