package com.example.facex.domain.usecase

import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectorFactory
import com.example.facex.domain.entities.FaceRecognizerFactory
import com.example.facex.domain.entities.ModelOption
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
class ChangeServiceModelUseCase @Inject constructor(
    private val processorFacade: FaceProcessorFacade,
    private val faceDetectorFactory: FaceDetectorFactory,
    private val faceRecognizerFactory: FaceRecognizerFactory,
    private val mlConfigurationRepository: MLConfigurationRepository,
    private val logger: Logger
) {
    /**
     * Changes the current model for a given service.
     *
     * @param serviceOption The service option (detection or recognition) whose model is changing.
     * @param newModel The newly selected model option.
     */
    suspend operator fun invoke(
        serviceOption: ServiceOption, newModel: ModelOption
    ): Result<Unit> {
        return when (val type = serviceOption.serviceType) {
            is ProcessorType.Detection -> updateDetectionServiceModel(type, newModel)
            is ProcessorType.Recognition -> updateRecognitionServiceModel(type, newModel)
        }
    }

    private suspend fun updateDetectionServiceModel(
        detectionType: ProcessorType.Detection, newModel: ModelOption
    ): Result<Unit> {
        // Obtain the current detection configuration.
        val currentConfig = mlConfigurationRepository.detectionConfig.value
        // Update the configuration with the new model path (if applicable).
        // For example, for MediaPipe, update the modelPath field.
        val updatedConfig = when (currentConfig) {
            is DetectionConfig.MediaPipe -> currentConfig.copy(modelPath = newModel.path)
            is DetectionConfig.MLKit -> currentConfig // MLKit might not support model switching.
            is DetectionConfig.OpenCV -> currentConfig.copy() // Adjust if OpenCV uses different model files.
        }
        // Create a new detector based on the updated configuration.
        return faceDetectorFactory.create(updatedConfig).mapCatching { detector ->
            processorFacade.updateFaceDetector(detector).getOrThrow()
            mlConfigurationRepository.updateDetectionConfig(updatedConfig)
            "Detection service model changed to '${newModel.name}' for $detectionType"
        }
    }

    private suspend fun updateRecognitionServiceModel(
        recognitionType: ProcessorType.Recognition, newModel: ModelOption
    ): Result<Unit> {
        // Obtain the current recognition configuration.
        val currentConfig = mlConfigurationRepository.recognitionConfig.value
        // Update the configuration with the new model path if applicable.
        // For example, for LiteRT, you might update the modelConfig.
        val updatedConfig = when (currentConfig) {
            is RecognitionConfig.LiteRT -> currentConfig.copy(
                modelConfig = currentConfig.modelConfig.copy(
                    modelPath = newModel.path
                )
            )

            is RecognitionConfig.OpenCV -> currentConfig.copy() // Adjust as needed.
        }
        // Create a new recognizer using the updated configuration.
        return faceRecognizerFactory.create(updatedConfig).mapCatching { recognizer ->
            processorFacade.updateFaceRecognition(recognizer).getOrThrow()
            mlConfigurationRepository.updateRecognitionConfig(updatedConfig)
            "Recognition service model changed to '${newModel.name}' for $recognitionType".logInfo(
                logger
            )
        }
    }
}
