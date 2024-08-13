package com.example.facex.ui.screens.camera_face_recognition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController


class RecognitionCoordinator(
    val viewModel: RecognitionViewModel,
) {
    val screenStateFlow = viewModel.stateFlow
}

@Composable
fun rememberRecognitionCoordinator(): RecognitionCoordinator {
    val viewModel: RecognitionViewModel = hiltViewModel()
    return remember(viewModel) {
        RecognitionCoordinator(viewModel = viewModel)
    }
}

