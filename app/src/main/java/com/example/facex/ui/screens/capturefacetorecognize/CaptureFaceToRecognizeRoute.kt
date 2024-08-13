package com.example.facex.ui.screens.capturefacetorecognize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun CaptureFaceToRecognizeRoute(
    coordinator: CaptureFaceToRecognizeCoordinator = rememberCaptureFaceToRecognizeCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsState(CaptureFaceToRecognizeState())

    // UI Actions
    val actions = rememberCaptureFaceToRecognizeActions(coordinator)

    // UI Rendering
    CaptureFaceToRecognizeScreen(uiState, actions)
}


@Composable
fun rememberCaptureFaceToRecognizeActions(coordinator: CaptureFaceToRecognizeCoordinator): CaptureFaceToRecognizeActions {
    return remember(coordinator) {
        CaptureFaceToRecognizeActions(

        )
    }
}