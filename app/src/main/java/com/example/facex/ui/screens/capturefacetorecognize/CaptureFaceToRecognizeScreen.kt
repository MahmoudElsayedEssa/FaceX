package com.example.facex.ui.screens.capturefacetorecognize

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CaptureFaceToRecognizeScreen(
    state: CaptureFaceToRecognizeState,
    actions: CaptureFaceToRecognizeActions,
) {
    // TODO UI Rendering
}

@Composable
@Preview(name = "CaptureFaceToRecognize")
private fun CaptureFaceToRecognizeScreenPreview() {
    CaptureFaceToRecognizeScreen(
        state = CaptureFaceToRecognizeState(),
        actions = CaptureFaceToRecognizeActions()
    )
}
