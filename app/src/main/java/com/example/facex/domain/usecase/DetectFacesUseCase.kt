package com.example.facex.domain.usecase

import android.util.Log
import com.example.facex.data.cropToBoundingBox
import com.example.facex.data.toGrayScale
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.repository.MLRepository
import com.example.facex.ui.FrameData
import com.example.facex.ui.utils.scale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
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
    private val performanceTracker: PerformanceTracker,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
//    operator fun invoke(faceData: FrameData): Flow<List<DetectedFace?>> = callbackFlow {
//        val timeTaken = measureNanoTime {
//            val scaledBitmap = faceData.bitmap.scale(0.25f)
//            mlRepository.detectFaces(scaledBitmap, faceData.rotationDegrees) { faces ->
//                launch {
//                    val detectedFaces = faces.map { face ->
//                        async(defaultDispatcher) {
//                            val croppedBitmap = faceData.bitmap.cropToBoundingBox(
//                                face.boundingBox.scale(4f),
//                                faceData.rotationDegrees
//                            )?.toGrayScale()
//                            croppedBitmap?.let {
//                                DetectedFace(
//                                    boundingBox = face.boundingBox,
//                                    trackedId = face.trackingId,
//                                    bitmap = it
//                                )
//                            }
//                        }
//                    }.awaitAll().filterNotNull()
//
//                    trySend(detectedFaces)
//                    close()
//                }
//            }
//        }
//        performanceTracker.updateMetric(PerformanceTracker.DETECTION_TIME, timeTaken)
//        awaitClose()
//    }


    suspend operator fun invoke(faceData: FrameData): List<DetectedFace?> {

        // Measure the time taken for the entire operation
        val detectedFaces: List<DetectedFace?>
        val timeTaken = measureNanoTime {
            // Downscale the image
            val scaledBitmap = faceData.bitmap.scale(0.25f)

            // Detect faces using coroutines
            val faces = mlRepository.detectFaces(scaledBitmap, faceData.rotationDegrees)

            Log.d("faces", "invoke:faces:$faces ")
            // Process each detected face using a thread pool
            detectedFaces = faces.map { face ->
                coroutineScope {
                    async(Dispatchers.Default) {
                        scaledBitmap.cropToBoundingBox(face.boundingBox, faceData.rotationDegrees)
                            ?.toGrayScale()?.let { croppedBitmap ->
                                DetectedFace(
                                    boundingBox = face.boundingBox.scale(4f), // Scale back to original size
                                    trackedId = face.trackingId,
                                    bitmap = croppedBitmap
                                )
                            }
                    }
                }
            }.awaitAll()
        }

        performanceTracker.updateMetric(PerformanceTracker.DETECTION_TIME, timeTaken)
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        Log.d("performanceTracker", "invoke:DETECTION_TIME:${formatter.format(timeTaken)} ")
        return detectedFaces
    }

}
