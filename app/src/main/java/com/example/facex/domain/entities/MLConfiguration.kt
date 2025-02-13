package com.example.facex.domain.entities

import android.content.Context
import com.example.facex.R
import com.example.facex.data.local.ml.MLRepository
import com.example.facex.data.local.ml.detection.MLKitFaceDetector
import com.example.facex.data.local.ml.detection.MediaPipeFaceDetector
import com.example.facex.data.local.ml.detection.OpenCVFaceDetector
import com.example.facex.data.local.ml.embeddingsgenerator.OpenCVFaceRecognizer
import com.example.facex.data.local.ml.embeddingsgenerator.lifeRT.LiteRTEmbeddingGenerator
import com.example.facex.data.local.ml.liteRT.modelhandling.LiteRTModelConfig
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.ml.EmbeddingGenerator
import com.example.facex.domain.ml.FaceDetector
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.dnn.Dnn
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ProcessorType {
    interface Detection : ProcessorType {
        object MediaPipe : Detection
        object MLKit : Detection
        object OpenCV : Detection
    }

    interface Recognition : ProcessorType {
        object OpenCV : Recognition
        object LiteRT : Recognition

    }
}


sealed interface MLConfiguration {
    val type: ProcessorType
    fun validate(): Result<Unit>
}

sealed interface DetectionConfig : MLConfiguration {
    data class MediaPipe(
        override val type: ProcessorType.Detection = ProcessorType.Detection.MediaPipe,
        val modelPath: String = "blaze_face_short_range.tflite",
        val minDetectionConfidence: Float = 0.5f,
        val delegate: Delegate = Delegate.CPU,
        val runningMode: RunningMode = RunningMode.IMAGE
    ) : DetectionConfig {
        override fun validate(): Result<Unit> = runCatching {
            require(modelPath.isNotEmpty()) { "Model path cannot be empty" }
            require(minDetectionConfidence in 0f..1f) { "Detection confidence must be between 0 and 1" }
        }
    }

    data class MLKit(
        override val type: ProcessorType.Detection = ProcessorType.Detection.MLKit,
        val performanceMode: Int = FaceDetectorOptions.PERFORMANCE_MODE_FAST,
        val minFaceSize: Float = 0.15f,
        val enableTracking: Boolean = true
    ) : DetectionConfig {
        override fun validate(): Result<Unit> = runCatching {
            require(minFaceSize in 0.1f..0.5f) { "Face size must be between 0.1 and 0.5" }
        }
    }

    data class OpenCV(
        override val type: ProcessorType.Detection = ProcessorType.Detection.OpenCV,
        val nmsThreshold: Float = 0.3f,
        val scoreThreshold: Float = 0.6f,
        val topK: Int = 5000,
        val imageSize: org.opencv.core.Size = org.opencv.core.Size(320.0, 320.0)
    ) : DetectionConfig {
        override fun validate(): Result<Unit> = runCatching {
            require(nmsThreshold in 0f..1f) { "NMS threshold must be between 0 and 1" }
            require(scoreThreshold in 0f..1f) { "Score threshold must be between 0 and 1" }
            require(topK > 0) { "TopK must be positive" }
            require(imageSize.width > 0 && imageSize.height > 0) { "Invalid image dimensions" }
        }
    }
}

sealed interface RecognitionConfig : MLConfiguration {
    data class OpenCV(
        override val type: ProcessorType.Recognition = ProcessorType.Recognition.OpenCV,
        val imageSize: org.opencv.core.Size = org.opencv.core.Size(112.0, 112.0),
        val backend: Int = Dnn.DNN_BACKEND_OPENCV,
        val target: Int = Dnn.DNN_TARGET_CPU
    ) : RecognitionConfig {
        override fun validate(): Result<Unit> = runCatching {
            require(imageSize.width > 0 && imageSize.height > 0) { "Invalid image dimensions" }
        }
    }

    data class LiteRT(
        override val type: ProcessorType.Recognition = ProcessorType.Recognition.LiteRT,
        val modelConfig: LiteRTModelConfig = LiteRTModelConfig(),
    ) : RecognitionConfig {
        override fun validate(): Result<Unit> = runCatching {
            with(modelConfig) {
                require(numThreads > 0) { "Thread count must be positive" }
                require(inputConfig.inputHeight > 0 && inputConfig.inputWidth > 0) { "Invalid input dimensions" }
                require(outputConfig.embeddingSize > 0) { "Invalid embedding size" }
            }
        }
    }
}


@Singleton
class FaceDetectorFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {
    fun create(config: DetectionConfig): Result<FaceDetector> = runCatching {
        when (config) {
            is DetectionConfig.MediaPipe -> createMediaPipeDetector(config)
            is DetectionConfig.MLKit -> createMLKitDetector(config)
            is DetectionConfig.OpenCV -> createOpenCVDetector(config)
        }
    }

    private fun createMediaPipeDetector(config: DetectionConfig.MediaPipe): FaceDetector {
        return MediaPipeFaceDetector(
            context = context,
            logger = logger,
            config = config
        )
    }

    private fun createMLKitDetector(config: DetectionConfig.MLKit): FaceDetector {
        return MLKitFaceDetector(
            faceDetectorOptions = FaceDetectorOptions.Builder()
                .setPerformanceMode(config.performanceMode)
                .setMinFaceSize(config.minFaceSize)
                .apply { if (config.enableTracking) enableTracking() }
                .build(),
            logger = logger
        )
    }

    private fun createOpenCVDetector(config: DetectionConfig.OpenCV): FaceDetector {
        // Example: Retrieve model from resources or repository as needed.
        val modelStream = context.resources.openRawResource(R.raw.face_detection_yunet_2023mar)
        return OpenCVFaceDetector(
            modelInputStream = modelStream,
            config = config
        )
    }
}

@Singleton
class FaceRecognizerFactory @Inject constructor(
    private val mlRepository: MLRepository,
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {
    fun create(
        config: RecognitionConfig
    ): Result<EmbeddingGenerator> = runCatching {
        when (config) {
            is RecognitionConfig.LiteRT -> {
                LiteRTEmbeddingGenerator(
                    mlRepository = mlRepository, config = config, logger = logger
                )
            }

            is RecognitionConfig.OpenCV -> {
                OpenCVFaceRecognizer(
                    modelInputStream = context.resources.openRawResource(R.raw.face_recognition_sface_2021dec),
                    config = config,
                    logger = logger
                )
            }

            else -> throw IllegalArgumentException("Mismatched config type for $config")

        }
    }

    private fun createOpenCVRecognizer(config: RecognitionConfig.OpenCV): EmbeddingGenerator {
        return OpenCVFaceRecognizer(
            modelInputStream = context.resources.openRawResource(R.raw.face_recognition_sface_2021dec),
            config = config,
            logger = logger
        )
    }

    private fun createLiteRTRecognizer(config: RecognitionConfig.LiteRT): EmbeddingGenerator {
        return LiteRTEmbeddingGenerator(
            mlRepository = mlRepository, config = config, logger = logger
        )
    }
}
