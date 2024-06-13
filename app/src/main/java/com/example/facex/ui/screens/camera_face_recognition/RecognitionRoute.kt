package com.example.facex.ui.screens.camera_face_recognition

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun rememberRecognitionActions(coordinator: RecognitionCoordinator): RecognitionActions {
    return remember(coordinator) {
        Log.d(
            "TAG",
            "rememberRecognitionActions:coordinator$coordinator,  viewModel${coordinator.viewModel} "
        )
        RecognitionActions(
            startCamera = coordinator.viewModel::onStartCamera,
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
    Log.d("TAG", "RecognitionRoute:uiState$uiState , actions$actions ")
    CameraRecognitionScreen(uiState, actions)
}