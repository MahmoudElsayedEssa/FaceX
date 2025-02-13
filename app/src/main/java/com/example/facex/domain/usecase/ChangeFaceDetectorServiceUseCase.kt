package com.example.facex.domain.usecase

import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectorFactory
import com.example.facex.domain.entities.FaceRecognizerFactory
import com.example.facex.domain.entities.ProcessorType
import com.example.facex.domain.entities.RecognitionConfig
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.repository.MLConfigurationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangeDetectionServiceUseCase @Inject constructor(
    private val processorFacade: FaceProcessorFacade,
    private val faceDetectorFactory: FaceDetectorFactory,
    private val mlConfigurationRepository: MLConfigurationRepository,
    private val logger: Logger
) {
    suspend fun execute(serviceOption: ServiceOption): Result<Unit> {
        // Ensure the service option is for detection.
        val detectionType = serviceOption.serviceType as? ProcessorType.Detection
            ?: return Result.failure(IllegalArgumentException("Service option is not a detection service"))

        // Create a new default configuration based on the selected detection type.
        val newConfig: DetectionConfig = when (detectionType) {
            ProcessorType.Detection.MediaPipe -> DetectionConfig.MediaPipe()
            ProcessorType.Detection.MLKit -> DetectionConfig.MLKit()
            ProcessorType.Detection.OpenCV -> DetectionConfig.OpenCV()
            else -> DetectionConfig.MLKit()
        }
        return faceDetectorFactory.create(newConfig).mapCatching { detector ->
            processorFacade.updateFaceDetector(detector).getOrThrow()
            mlConfigurationRepository.updateDetectionConfig(newConfig)
            "Detection service switched to $detectionType".logInfo(logger)
        }
    }
}


@Singleton
class ChangeRecognitionServiceUseCase @Inject constructor(
    private val processorFacade: FaceProcessorFacade,
    private val faceRecognizerFactory: FaceRecognizerFactory,
    private val mlConfigurationRepository: MLConfigurationRepository,
    private val logger: Logger
) {
    suspend fun execute(serviceOption: ServiceOption): Result<Unit> {
        // Ensure the service option is for recognition.
        val recognitionType = serviceOption.serviceType as? ProcessorType.Recognition
            ?: return Result.failure(IllegalArgumentException("Service option is not a recognition service"))

        // Create a new default configuration based on the selected recognition type.
        val newConfig: RecognitionConfig = when (recognitionType) {
            ProcessorType.Recognition.LiteRT -> RecognitionConfig.LiteRT()
            ProcessorType.Recognition.OpenCV -> RecognitionConfig.OpenCV()
            else -> RecognitionConfig.LiteRT()
        }
        return faceRecognizerFactory.create(newConfig).mapCatching { recognizer ->
            processorFacade.updateFaceRecognition(recognizer).getOrThrow()
            mlConfigurationRepository.updateRecognitionConfig(newConfig)
            "Recognition service switched to $recognitionType".logInfo(logger)
        }
    }
}


