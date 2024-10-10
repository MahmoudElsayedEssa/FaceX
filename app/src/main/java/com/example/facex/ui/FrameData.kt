package com.example.facex.ui

import com.example.facex.domain.entities.ImageInput

data class FrameData(
    val id: Long,
    val rotationDegrees: Int,
    val imageInput: ImageInput
)