package com.example.facex.data.local.ml

import com.example.facex.data.local.db.ModelConfigDTO
import com.example.facex.data.local.ml.liteRT.InputProcessor
import com.example.facex.data.local.ml.liteRT.OutputProcessor
import com.example.facex.data.local.ml.liteRT.TFLiteInterpreterWrapper
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelType
import com.example.facex.domain.entities.RecognitionConfig
import java.nio.MappedByteBuffer

interface MLRepository {
    suspend fun getModelConfig(modelId: Long): Result<Pair<RecognitionConfig.LiteRT, MappedByteBuffer>>
    suspend fun <T, R> getInterpreter(
        modelId: Long,
        inputProcessor: InputProcessor<T>,
        outputProcessor: OutputProcessor<R>
    ): Result<TFLiteInterpreterWrapper<T, R>>

    suspend fun releaseInterpreter(modelId: Long)
    suspend fun releaseAllInterpreters()
    suspend fun getDefaultModelConfigByType(modelType: ModelType): ModelConfigDTO?
}

