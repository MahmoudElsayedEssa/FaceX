package com.example.facex.domain.repository

import android.content.Context
import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.ModelOption
import com.example.facex.domain.entities.ProcessorType
import com.example.facex.domain.entities.RecognitionConfig
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLConfigurationRepository @Inject constructor(
    private val logger: Logger
) {
    // The current configuration flows
    private val _detectionConfig = MutableStateFlow<DetectionConfig>(DetectionConfig.MediaPipe())
    val detectionConfig: StateFlow<DetectionConfig> = _detectionConfig.asStateFlow()

    private val _recognitionConfig = MutableStateFlow<RecognitionConfig>(RecognitionConfig.LiteRT())
    val recognitionConfig: StateFlow<RecognitionConfig> = _recognitionConfig.asStateFlow()

    // Available services – these might be static or dynamically updated
    private val _detectionServices = MutableStateFlow(
        listOf(
            ServiceOption(
                id = 1,
                name = "ML Kit",
                description = "Google's On-Device ML Kit",
                serviceType = ProcessorType.Detection.MLKit,
                models = null,
                isCurrent = true
            ),
            ServiceOption(
                id = 2,
                name = "MediaPipe",
                description = "MediaPipe Face Detection",
                serviceType = ProcessorType.Detection.MediaPipe,
                models = listOf(
                    ModelOption(
                        id = 1,
                        name = "Blaze Face",
                        path = "blaze_face_short_range.tflite",
                        description = "Blaze Face for short range",
                        isCurrent = true
                    )
                )
            ),
            ServiceOption(
                id = 3,
                name = "OpenCV",
                description = "OpenCV Face Detection",
                serviceType = ProcessorType.Detection.OpenCV,
                models = listOf(
                    ModelOption(
                        id = 2,
                        name = "Yu-net",
                        path = "face_detection_yunet_2023mar.onnx",
                        description = "Yu-net for detection",
                        isCurrent = true
                    )
                )
            )
        )
    )
    val detectionServices: StateFlow<List<ServiceOption>> = _detectionServices.asStateFlow()

    private val _recognitionServices = MutableStateFlow(
        listOf(
            ServiceOption(
                id = 4,
                name = "LiteRT",
                description = "TFLite Models",
                serviceType = ProcessorType.Recognition.LiteRT,
                models = listOf(
                    ModelOption(
                        id = 3,
                        name = "MobileFaceNet 512",
                        path = "ms1m_mobilenetv2_16.tflite",
                        description = "MobileFaceNet 512 Embeddings",
                        isCurrent = true,
                        threshold = 0.7f
                    ),
//                    ModelOption(
//                        id = 3,
//                        name = "MobileFaceNet 192",
//                        path = "mobile_face_net.tflite",
//                        description = "MobileFaceNet 512 Embeddings",
//                        isCurrent = true,
//                        threshold = 0.5f
//                    )

                ),
                isCurrent = true
            ),
            ServiceOption(
                id = 5,
                name = "OpenCV",
                description = "ONNX Models",
                serviceType = ProcessorType.Recognition.OpenCV,
                models = listOf(
                    ModelOption(
                        id = 5,
                        name = "S-Face",
                        path = "face_recognition_sface_2021dec.onnx",
                        description = "",
                        isCurrent = true
                    )
                )
            )
        )
    )
    val recognitionServices: StateFlow<List<ServiceOption>> = _recognitionServices.asStateFlow()

    // Methods to update configurations
    fun updateDetectionConfig(newConfig: DetectionConfig) {
        newConfig.validate().getOrElse { throw it }
        _detectionConfig.value = newConfig.logInfo(logger) { "Updated detection config: $it" }
    }

    fun updateRecognitionConfig(newConfig: RecognitionConfig) {
        newConfig.validate().getOrElse { throw it }
        _recognitionConfig.value = newConfig.logInfo(logger) { "Updated recognition config: $it" }
    }

    // Optionally, methods to update the service lists (for example, reordering)
    fun updateDetectionServices(newList: List<ServiceOption>) {
        _detectionServices.value = newList
    }

    fun updateRecognitionServices(newList: List<ServiceOption>) {
        _recognitionServices.value = newList
    }
}
