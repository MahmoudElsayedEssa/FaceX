package com.example.facex.ui.helpers.processors.formats

import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.ImageInput
import com.example.facex.ui.helpers.models.ImageOutputType

interface ImageFormatProcessor {
    suspend fun process(image: ImageProxy, outputType: ImageOutputType): ImageInput
}