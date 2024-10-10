package com.example.facex.ui.helpers.operations

import com.example.facex.domain.entities.ImageInput

fun interface ImageOperation {
    suspend fun process(image: ImageInput): ImageInput
}

