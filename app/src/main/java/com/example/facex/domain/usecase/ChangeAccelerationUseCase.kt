package com.example.facex.domain.usecase

import com.example.facex.data.local.db.ModelConfigDao
import com.example.facex.data.local.ml.MLRepository
import com.example.facex.data.local.ml.liteRT.DelegateType
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelType
import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectorFactory
import com.example.facex.domain.entities.FaceRecognizerFactory
import com.example.facex.domain.entities.ModelAcceleration
import com.example.facex.domain.entities.ProcessorType
import com.example.facex.domain.entities.RecognitionConfig
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.repository.MLConfigurationRepository
import com.google.mediapipe.tasks.core.Delegate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangeAccelerationUseCase @Inject constructor(
    private val processorFacade: FaceProcessorFacade,
    private val faceDetectorFactory: FaceDetectorFactory,
    private val faceRecognizerFactory: FaceRecognizerFactory,
    private val modelConfigDao: ModelConfigDao,
    private val mlRepository: MLRepository,
    private val mlConfigurationRepository: MLConfigurationRepository,
    private val logger: Logger
) {
    suspend operator fun invoke(
        serviceOption: ServiceOption,
        acceleration: ModelAcceleration
    ): Result<Unit> {
        return when (val type = serviceOption.serviceType) {
            is ProcessorType.Detection -> updateDetectionAcceleration(type, acceleration)
            is ProcessorType.Recognition -> updateRecognitionAcceleration(type, acceleration)
        }
    }

    private suspend fun updateDetectionAcceleration(
        detectionType: ProcessorType.Detection,
        acceleration: ModelAcceleration
    ): Result<Unit> {
        // Get the current detection config from the repository.
        val currentConfig = mlConfigurationRepository.detectionConfig.value

        // For example, only MediaPipe supports acceleration changes.
        val updatedConfig = try{
            when (currentConfig) {
                is DetectionConfig.MediaPipe -> currentConfig.copy(
                    delegate = acceleration.toMediaPipeDelegate()
                )

                else -> return Result.failure(
                    IllegalArgumentException("Acceleration change not supported for $detectionType")
                )
            }
        }
        catch (e:Exception){
            return Result.failure(
                IllegalArgumentException("Acceleration change not supported for $detectionType")
            )
        }
        // Create a new detector using the updated configuration.
        return faceDetectorFactory.create(updatedConfig).mapCatching { detector ->
            processorFacade.updateFaceDetector(detector).getOrThrow()
            mlConfigurationRepository.updateDetectionConfig(updatedConfig)
            "Detection acceleration updated for $detectionType".logInfo(logger)
        }
    }

    private suspend fun updateRecognitionAcceleration(
        recognitionType: ProcessorType.Recognition,
        acceleration: ModelAcceleration
    ): Result<Unit> {
        // Get the current recognition config.
        val currentConfig = mlConfigurationRepository.recognitionConfig.value

        // For example, only LiteRT supports acceleration changes.
        val updatedConfig = when (currentConfig) {
            is RecognitionConfig.LiteRT -> {

                val defaultConfig =
                    mlRepository.getDefaultModelConfigByType(ModelType.EMBEDDING_GENERATION)
                        ?: throw IllegalStateException("No default embedding model found")


                modelConfigDao.updateModelConfig(defaultConfig.copy(delegate = acceleration.toLiteRTDelegate().name))

                currentConfig.copy(
                    modelConfig = currentConfig.modelConfig.copy(
                        delegate = acceleration.toLiteRTDelegate()
                    )
                )
            }

            else -> return Result.failure(
                IllegalArgumentException("Acceleration change not supported for $recognitionType")
            )
        }
        // Create a new recognizer and update the processor.
        return faceRecognizerFactory.create(updatedConfig).mapCatching { recognizer ->
            processorFacade.updateFaceRecognition(recognizer).getOrThrow()
            mlConfigurationRepository.updateRecognitionConfig(updatedConfig)
            "Recognition acceleration updated for $recognitionType".logInfo(logger)
        }
    }
}

fun ModelAcceleration.toMediaPipeDelegate(): Delegate {
    return when (this) {
        ModelAcceleration.CPU -> Delegate.CPU
        ModelAcceleration.GPU -> Delegate.GPU
        ModelAcceleration.NNAPI -> {
            throw IllegalStateException("MediaPipe does not support NNAPI acceleration")
        }
    }
}

fun ModelAcceleration.toLiteRTDelegate(): DelegateType = when (this) {
    ModelAcceleration.CPU -> DelegateType.CPU
    ModelAcceleration.GPU -> DelegateType.GPU
    ModelAcceleration.NNAPI -> DelegateType.NNAPI
}
