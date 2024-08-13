package com.example.facex.ui.screens.capturefacetorecognize

import android.graphics.Bitmap

/**
 * UI State that represents CaptureFaceToRecognizeScreen
 **/
data class CaptureFaceToRecognizeState(
    val bitmap: Bitmap? = null
)

/**
 * CaptureFaceToRecognize Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/
data class CaptureFaceToRecognizeActions(
    val onClick: () -> Unit = {}
)


