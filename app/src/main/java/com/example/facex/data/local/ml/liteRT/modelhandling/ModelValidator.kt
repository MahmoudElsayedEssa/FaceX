package com.example.facex.data.local.ml.liteRT.modelhandling

import java.nio.MappedByteBuffer

fun interface ModelValidator {
    fun validate(modelFile: MappedByteBuffer, config: LiteRTModelConfig): Boolean
}
