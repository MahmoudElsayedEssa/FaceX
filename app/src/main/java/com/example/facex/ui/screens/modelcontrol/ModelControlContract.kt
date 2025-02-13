package com.example.facex.ui.screens.modelcontrol

import com.example.facex.domain.entities.ModelAcceleration
import com.example.facex.domain.entities.ModelOption
import com.example.facex.domain.entities.ServiceOption


data class ModelControlState(
    val isLoading: Boolean = false,
    val detectionServices: List<ServiceOption> = emptyList(),
    val recognitionServices: List<ServiceOption> = emptyList(),
    val selectedDetectionIndex: Int = -1,
    val selectedRecognitionIndex: Int = -1,
    val lastError: String? = null

)

//fun ServiceType.asFaceDetectionService(): FaceDetectionService {
//    return when (this) {
//        ServiceType.ML_KIT -> FaceDetectionService.ML_KIT
//        ServiceType.MEDIA_PIPE -> FaceDetectionService.MEDIA_PIPE
//        ServiceType.OPEN_CV -> FaceDetectionService.OPENCV
//        ServiceType.TENSOR_FLOW -> FaceDetectionService.LITE_RT
//    }
//}

//fun ServiceType.asFaceRecognitionService(): FaceRecognitionService {
//    return when (this) {
//        ServiceType.OPEN_CV -> FaceRecognitionService.OPENCV
//        ServiceType.TENSOR_FLOW -> FaceRecognitionService.LITE_RT
//        else -> FaceRecognitionService.LITE_RT
//    }
//}


data class ModelControlActions(
    val onSwitchDetectionService: (ServiceOption) -> Unit = {},
    val onSwitchRecognitionService: (ServiceOption) -> Unit = { _ -> },
    val onChangeServiceModel: (ServiceOption, ModelOption) -> Unit = { _, _ -> },
    val onRecognitionChangeThreshold: (serviceOption: ServiceOption, newThreshold: Float) -> Unit = { _, _ -> },
    val moveRecognitionServiceToTop: (selectedItem: ServiceOption) -> Unit = {},
    val moveDetectionServiceToTop: (selectedItem: ServiceOption) -> Unit = {},
    val onChangeAcceleration: (serviceOption: ServiceOption, newAcceleration: ModelAcceleration) -> Unit = { _, _ -> },
    val onDetectionChangeThreshold: (serviceOption: ServiceOption, newThreshold: Float) -> Unit = { _, _ -> },
)