package com.example.facex.ui.screens.capturefacetorecognize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen's coordinator which is responsible for handling actions from the UI layer
 * and one-shot actions based on the new UI state
 */
class CaptureFaceToRecognizeCoordinator(
    val viewModel: CaptureFaceToRecognizeViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun doStuff() {
        // TODO Handle UI Action
    }
}

@Composable
fun rememberCaptureFaceToRecognizeCoordinator(
    viewModel: CaptureFaceToRecognizeViewModel = hiltViewModel()
): CaptureFaceToRecognizeCoordinator {
    return remember(viewModel) {
        CaptureFaceToRecognizeCoordinator(
            viewModel = viewModel
        )
    }
}