package com.example.facex.domain.usecase

import android.util.Log
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.toGrayScale
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.repository.MLRepository
import com.example.facex.ui.FrameData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureNanoTime


@Singleton
class DetectFacesUseCase @Inject constructor(
    private val mlRepository: MLRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(faceData: FrameData): Flow<List<DetectedFace?>> = callbackFlow {
        val timeTaken = measureNanoTime {
            mlRepository.detectFaces(faceData.bitmap, faceData.rotationDegrees) { faces ->
                launch {
                    val detectedFaces = faces.mapNotNull { face ->
                        async(defaultDispatcher) {
                            val croppedBitmap = faceData.bitmap.cropToBoundingBox(
                                face.boundingBox,
                                faceData.rotationDegrees
                            )?.toGrayScale()
                            croppedBitmap?.let {
                                DetectedFace(
                                    boundingBox = face.boundingBox,
                                    trackedId = face.trackingId,
                                    bitmap = it
                                )
                            }
                        }
                    }.awaitAll().filterNotNull()

                    trySend(detectedFaces)
                    close()
                }
            }
        }

        // Format the measured time with commas
        val formattedNanoTime = NumberFormat.getNumberInstance(Locale.US).format(timeTaken)

        Log.d(
            "MAMO",
            "Time taken to detect faces: $formattedNanoTime ns )"
        )

        awaitClose()
    }
}
