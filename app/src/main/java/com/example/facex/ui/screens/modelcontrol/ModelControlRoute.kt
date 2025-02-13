package com.example.facex.ui.screens.modelcontrol

import ModelControlScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun ModelControlRoute(
    viewModel: ModelControlViewModel = hiltViewModel(),
    expandedRatioProvider: () -> Float = { 1f },
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val actions = rememberModelControlActions(viewModel)

    ModelControlScreen(uiState, actions,expandedRatioProvider = expandedRatioProvider)
}


@Composable
fun rememberModelControlActions(viewModel: ModelControlViewModel): ModelControlActions {
    return remember(viewModel) {
        ModelControlActions(
            onSwitchDetectionService = viewModel::switchDetectionService,
            onSwitchRecognitionService = viewModel::switchRecognitionService,
            onChangeServiceModel = viewModel::changeServiceModel,
            onDetectionChangeThreshold = viewModel::changeDetectionThreshold,
            onRecognitionChangeThreshold = viewModel::changeRecognitionThreshold,
            onChangeAcceleration = viewModel::changeAcceleration,
            moveRecognitionServiceToTop = viewModel::moveRecognitionServiceToTop,
            moveDetectionServiceToTop = viewModel::moveDetectionServiceToTop
        )
    }
}
