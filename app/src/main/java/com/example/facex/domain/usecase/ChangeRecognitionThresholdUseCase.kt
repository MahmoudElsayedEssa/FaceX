package com.example.facex.domain.usecase

import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectorFactory
import com.example.facex.domain.entities.FaceRecognizerFactory
import com.example.facex.domain.entities.ModelOption
import com.example.facex.domain.entities.ProcessorType
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.repository.MLConfigurationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangeRecognitionThresholdUseCase @Inject constructor(
    private val processorFacade: FaceProcessorFacade,
    private val faceDetectorFactory: FaceDetectorFactory,
    private val faceRecognizerFactory: FaceRecognizerFactory,
    private val mlConfigurationRepository: MLConfigurationRepository,
    private val findBestMatchUseCase: FindBestMatchUseCase,
    private val logger: Logger
) {
    /**
     * Updates the threshold for a given service.
     *
     * @param serviceOption The service option (detection or recognition) being updated.
     * @param model The model option (e.g. which model’s threshold is being updated).
     * @param newThreshold The new threshold value.
     */
    suspend operator fun invoke(
        serviceOption: ServiceOption,
        newThreshold: Float
    ): Result<Unit> {
        return when (val type = serviceOption.serviceType) {
            is ProcessorType.Detection -> updateDetectionThreshold(type, newThreshold)
            is ProcessorType.Recognition -> updateRecognitionThreshold(type, newThreshold)
        }
    }

    private suspend fun updateDetectionThreshold(
        detectionType: ProcessorType.Detection,
        newThreshold: Float
    ): Result<Unit> {
        // Obtain the current detection configuration.
        val currentConfig = mlConfigurationRepository.detectionConfig.value
        // Update the configuration based on the actual type.
        val updatedConfig = when (currentConfig) {
            is DetectionConfig.MediaPipe -> currentConfig.copy(minDetectionConfidence = newThreshold)
            is DetectionConfig.MLKit -> currentConfig.copy(minFaceSize = newThreshold)
            is DetectionConfig.OpenCV -> currentConfig.copy(nmsThreshold = newThreshold)
        }
        // Use the detector factory to create a new detector with the updated configuration.
        return faceDetectorFactory.create(updatedConfig).mapCatching { detector ->
            // Update the ML processor.
            processorFacade.updateFaceDetector(detector).getOrThrow()
            // Persist the new configuration.
            mlConfigurationRepository.updateDetectionConfig(updatedConfig)
            "Detection threshold updated for $detectionType".logInfo(logger)
        }
    }

    private suspend fun updateRecognitionThreshold(
        recognitionType: ProcessorType.Recognition,
        newThreshold: Float
    ): Result<Unit> {
        // For recognition, if the threshold isn’t part of the configuration,
        // you might delegate to another use case or simply update other state.
        findBestMatchUseCase.updateConfig(newThreshold)
        // Optionally, if you want to update the recognition config:
        val currentConfig = mlConfigurationRepository.recognitionConfig.value
        // In this simple example, we assume the recognition config isn’t changed.
        // (Alternatively, you could add a threshold parameter in your recognition config.)
        return faceRecognizerFactory.create(currentConfig).mapCatching { recognizer ->
            processorFacade.updateFaceRecognition(recognizer).getOrThrow()
            // No configuration update is performed here because threshold might be managed elsewhere.
            "Recognition threshold updated for $recognitionType".logInfo(logger)
        }
    }
}
