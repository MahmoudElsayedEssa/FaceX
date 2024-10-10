package com.example.facex.ui.helpers.processors.conversion

import com.example.facex.domain.entities.ImageInput
import com.example.facex.ui.helpers.models.ImageOutputType


fun interface ImageFormatConversion {
    suspend fun convert(image: ImageInput, outputType: ImageOutputType): ImageInput
}
