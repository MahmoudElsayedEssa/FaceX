package com.example.facex.domain.usecase

import android.util.Log
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.toGrayScale
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.repository.MLRepository
import com.example.facex.ui.FrameData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class DetectFacesUseCase @Inject constructor(
    private val mlRepository: MLRepository,
) {
    operator fun invoke(faceData: FrameData): Flow<List<DetectedFace?>> =
        callbackFlow {
            val timeTaken = measureTimeMillis {
                mlRepository.detectFaces(faceData.bitmap, faceData.rotationDegrees) { faces ->
                    Log.d(
                        "NOOO",
                        "updateDetectedFaces: bitmap size = ${faceData.bitmap.width} ${faceData.bitmap.height}"
                    )

                    val detectedFaces = faces.map { face ->
                        val croppedBitmap =
                            faceData.bitmap.cropToBoundingBox(
                                face.boundingBox,
                                faceData.rotationDegrees
                            )
                                ?.toGrayScale()
                        croppedBitmap?.let {
                            DetectedFace(
                                boundingBox = face.boundingBox,
                                trackedId = face.trackingId,
                                bitmap = it
                            )
                        }
                    }
                    trySend(detectedFaces)
                    close()
                }
            }
            Log.d("MAMO", "Time taken to detect faces:: $timeTaken ms")
            awaitClose()
        }
}

