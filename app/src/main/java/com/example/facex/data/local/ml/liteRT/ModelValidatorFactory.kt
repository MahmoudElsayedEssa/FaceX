package com.example.facex.data.local.ml.liteRT

import com.example.facex.data.local.ml.embeddingsgenerator.lifeRT.FaceEmbeddingModelValidator
import com.example.facex.data.local.ml.liteRT.modelhandling.LiteRTModelConfig
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelType
import java.nio.MappedByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelValidatorFactory @Inject constructor(
    private val faceEmbeddingValidator: FaceEmbeddingModelValidator
) {
    fun getValidator(modelType: ModelType, config: LiteRTModelConfig): (MappedByteBuffer) -> Boolean =
        when (modelType) {
            ModelType.EMBEDDING_GENERATION -> { file ->
                faceEmbeddingValidator.validate(file, config)
            }

            ModelType.FACE_DETECTION -> TODO()
        }
}