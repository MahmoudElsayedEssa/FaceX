package com.example.facex.ui.screens.camera_face_recognition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun rememberRecognitionActions(coordinator: RecognitionCoordinator): RecognitionActions {
    return remember(coordinator) {

        RecognitionActions(
            startCamera = coordinator.viewModel::startCamera,
            onCaptureFace = coordinator.viewModel::onRegisterPerson,
            onStopRecognition = coordinator.viewModel::onStopRecognition,
//            onTapToFocus = coordinator.viewModel::onHandleTapToFocus,
            switchCamera = coordinator.viewModel::onSwitchCamera,
            onLinearZoom = coordinator.viewModel::onLinearZoom,
            onZoomRatio = coordinator.viewModel::onZoomRatio,
            getRatioZoom = coordinator.viewModel::onZoomRatio,
            getLinearZoom = coordinator.viewModel::getLinearZoom,
//            navigateToUploadScreen = coordinator::navigateToUploadScreen
        )
    }
}

@Composable
fun RecognitionRoute(
//    navController: NavController,
    coordinator: RecognitionCoordinator = rememberRecognitionCoordinator(),
) {
    val uiState by coordinator.screenStateFlow.collectAsState(RecognitionState())
    val actions = rememberRecognitionActions(coordinator)
    CameraRecognitionScreen(uiState, actions)
}