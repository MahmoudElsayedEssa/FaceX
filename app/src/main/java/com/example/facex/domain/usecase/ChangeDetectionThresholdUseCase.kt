package com.example.facex.domain.usecase

import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectorFactory
import com.example.facex.domain.entities.ProcessorType
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.repository.MLConfigurationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangeDetectionThresholdUseCase @Inject constructor(
    private val processorFacade: FaceProcessorFacade,
    private val faceDetectorFactory: FaceDetectorFactory,
    private val mlConfigurationRepository: MLConfigurationRepository,
    private val logger: Logger
) {
    suspend operator fun invoke(
        serviceOption: ServiceOption,
        newThreshold: Float
    ): Result<Unit> {
        // Validate that the service option is for detection.
        val detectionType = serviceOption.serviceType as? ProcessorType.Detection
            ?: return Result.failure(IllegalArgumentException("Service option is not a detection service"))

        // Retrieve the current detection configuration.
        val currentConfig = mlConfigurationRepository.detectionConfig.value

        // Create an updated configuration based on the specific type.
        val updatedConfig = when (currentConfig) {
            is DetectionConfig.MediaPipe -> currentConfig.copy(minDetectionConfidence = newThreshold)
            is DetectionConfig.MLKit -> currentConfig.copy(minFaceSize = newThreshold)
            is DetectionConfig.OpenCV -> currentConfig.copy(nmsThreshold = newThreshold)
        }

        // Use the detector factory to create a new detector with the updated configuration.
        return faceDetectorFactory.create(updatedConfig).mapCatching { detector ->
            processorFacade.updateFaceDetector(detector).getOrThrow()
            mlConfigurationRepository.updateDetectionConfig(updatedConfig)
            "Detection threshold updated for $detectionType".logInfo(logger)
        }
    }
}

