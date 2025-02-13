package com.example.facex.ui.screens.camera_face_recognition.components.fabs

import androidx.compose.runtime.Composable

data class FabContentItem(
    val content: @Composable (toggleAnimation: Int) -> Unit
)
