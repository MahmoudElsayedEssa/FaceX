package com.example.facex.ui.screens.camera_face_recognition

import androidx.lifecycle.LifecycleOwner
import com.example.facex.domain.entities.CameraState
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.RecognizedPerson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class RecognitionState(
    val recognizedFaces: List<RecognizedPerson> = emptyList(),
    val detectedFaces: List<DetectedFace> = emptyList(),
    val cameraState: CameraState = CameraState.Inactive,

    )


data class RecognitionActions(
    val startCamera: (lifecycleOwner: LifecycleOwner) -> Unit = { },
    val navigateToUploadScreen: () -> Unit = {},
    val onCaptureFace: (name: String, embedding: Embedding) -> Unit = { _, _ -> },
    val onStopRecognition: () -> Unit = {},
    val onLinearZoom: (Float) -> Unit = {},
    val onZoomRatio: (Float) -> Unit = {},
    val onTapToFocus: (Float, Float) -> Unit = { _, _ -> },
    val switchCamera: (lifecycleOwner: LifecycleOwner) -> Unit = {},
    val getLinearZoom: () -> StateFlow<Float> = { MutableStateFlow(0f) },
    val getRatioZoom: () -> StateFlow<Float> = { MutableStateFlow(1f)},
)