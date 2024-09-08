package com.example.facex.ui.screens.camera_face_recognition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.facex.ui.FrameData


inline fun analyzeFrame(noinline action: (FrameData) -> Unit): (FrameData) -> Unit = action

@Composable
fun rememberRecognitionActions(coordinator: RecognitionCoordinator): RecognitionActions {
    return remember(coordinator) {

        RecognitionActions(
            onCaptureFace = coordinator.viewModel::onRegisterPerson,
            onStopRecognition = coordinator.viewModel::onStopRecognition,
            onAnalysis =
            coordinator.viewModel::onAnalysis

        )
    }
}

@Composable
fun RecognitionRoute(
    coordinator: RecognitionCoordinator = rememberRecognitionCoordinator(),
) {
    val uiState by coordinator.screenStateFlow.collectAsState(RecognitionState())
    val actions = rememberRecognitionActions(coordinator)
    CameraRecognitionScreen(uiState, actions)
}