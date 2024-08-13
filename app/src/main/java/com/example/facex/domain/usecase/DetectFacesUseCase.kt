package com.example.facex.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.toGrayScale
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.repository.MLRepository
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
    operator fun invoke(bitmap: Bitmap, rotationDegrees: Int): Flow<List<DetectedFace>> =
        callbackFlow {
            val timeTaken = measureTimeMillis {
                mlRepository.detectFaces(bitmap, rotationDegrees) { faces ->
                    val detectedFaces = faces.map { face ->
                        val croppedBitmap =
                            bitmap.cropToBoundingBox(face.boundingBox, rotationDegrees)
                                .toGrayScale()
                        DetectedFace(
                            boundingBox = face.boundingBox,
                            trackedId = face.trackingId,
                            bitmap = croppedBitmap
                        )
                    }
                    trySend(detectedFaces)
                    close()
                }
            }
            Log.d("MAMO", "Time taken to detect faces:: $timeTaken ms")
            awaitClose()
        }
}

