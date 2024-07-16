package com.example.facex.domain.entities

import androidx.camera.core.Preview

sealed class CameraState {
    object Inactive : CameraState()
    data class Active(val preview: Preview) : CameraState()
    data class Error(val message: String) : CameraState()
}