package com.example.facex.ui.screens.camera_face_recognition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController


class RecognitionCoordinator(
    val viewModel: RecognitionViewModel,
//    val navController: NavController,
) {
    val screenStateFlow = viewModel.stateFlow
//    fun navigateToUploadScreen() = navController.navigate(Screen.UploadImageRoute.route)

}

@Composable
fun rememberRecognitionCoordinator(): RecognitionCoordinator {
//    val navBackStackEntry = navController.currentBackStackEntry
    val viewModel: RecognitionViewModel = hiltViewModel()
    return remember(viewModel) {
        RecognitionCoordinator(viewModel = viewModel)
    }
}

