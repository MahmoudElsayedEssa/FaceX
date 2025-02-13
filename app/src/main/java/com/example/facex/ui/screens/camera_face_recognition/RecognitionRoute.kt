package com.example.facex.ui.screens.camera_face_recognition

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun rememberRecognitionActions(viewModel: RecognitionViewModel): RecognitionActions =
    remember(viewModel) {
        RecognitionActions(
            onRegisterFace = viewModel::registerFace,
            onTapFace = viewModel::onTapFace,
            onDismissDialog = viewModel::dismissDialog,
            onShowPerformanceBottomSheet = viewModel::showPerformanceSheet,
            onShowModelControlBottomSheet = viewModel::onShowModelControlBottomSheet,
            onStopRecognition = viewModel::cleanup,
            onCreateAnalyzer = viewModel::createAnalyzer,
            onClearMetrics = viewModel::onClearMetrics,
            onShowFaceDetailsDialog = viewModel::showFaceDetailsDialog,
            onShowRegistrationDialog = viewModel::showRegistrationDialog,

        )
    }

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun RecognitionRoute() {

    val viewModel: RecognitionViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val actions = rememberRecognitionActions(viewModel)

    CameraRecognitionScreen(uiState, actions)
}