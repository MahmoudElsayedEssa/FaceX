package com.example.facex.data.local.ml.detection


import android.content.Context
import androidx.core.graphics.toRect
import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.logger.logWarning
import com.google.mediapipe.framework.image.ByteBufferImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class MediaPipeFaceDetector(
    private val context: Context,
    private val logger: Logger,
    private val config: DetectionConfig.MediaPipe
) : com.example.facex.domain.ml.FaceDetector {

    private var faceDetector: FaceDetector? = null

    init {
        setupFaceDetector()
    }

    private fun setupFaceDetector() {
        try {
            faceDetector = createFaceDetector(config.delegate)
            "MediaPipe Face Detector initialized with ${config.delegate}".logInfo(logger)
        } catch (e: RuntimeException) {
            "Failed to initialize with ${config.delegate}, falling back to CPU: ${e.message}".logWarning(
                logger
            )
            faceDetector = createFaceDetector(Delegate.CPU)
            "MediaPipe Face Detector initialized with CPU".logInfo(logger)
        }
    }

    private fun createFaceDetector(delegate: Delegate): FaceDetector {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(config.modelPath)
            .setDelegate(delegate)
            .build()

        val options = FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinDetectionConfidence(config.minDetectionConfidence)
            .setRunningMode(config.runningMode)
            .build()

        return FaceDetector.createFromOptions(context, options)
    }


    override suspend fun detectFaces(image: Frame): Result<List<FaceDetectionResult>> =
        runCatching {
            coroutineScope {
                val rotatedImage = if (image.rotationDegrees != 0) {
                    image.rotate(image.rotationDegrees)
                } else {
                    image
                }

                val mpImage = ByteBufferImageBuilder(
                    rotatedImage.buffer,
                    rotatedImage.width,
                    rotatedImage.height,
                    MPImage.IMAGE_FORMAT_RGBA
                ).build()

                val detections = async {
                    val detector = faceDetector
                        ?: throw IllegalStateException("Face detector not initialized")
                    detector.detect(mpImage).detections().map { detection ->
                        val box = detection.boundingBox()
                        FaceDetectionResult(
//                            boundingBox = Rect(
//                                box.left.toInt(),
//                                 box.top.toInt(),
//                                 box.width().toInt(),
//                                 box.height().toInt()
//                            ),
                            boundingBox = box.toRect(),
                            trackingId = detection.keypoints().hashCode()
                        )
                    }
                }

                detections.await()
            }
        }.onFailure {
            "Face detection failed: ${it.message}".logWarning(logger)
        }


    override suspend fun close() {
        try {
            faceDetector?.close()
            faceDetector = null
            "MediaPipe Face Detector closed successfully".logInfo(logger)
        } catch (e: Exception) {
            "Failed to close MediaPipe Face Detector: ${e.message}".logWarning(logger)
        }
    }

}
