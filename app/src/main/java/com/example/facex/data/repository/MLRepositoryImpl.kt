package com.example.facex.data.repository

import com.example.facex.data.local.db.ModelConfigDTO
import com.example.facex.data.local.db.ModelConfigDao
import com.example.facex.data.local.ml.MLRepository
import com.example.facex.data.local.ml.ModelStorageManager
import com.example.facex.data.local.ml.liteRT.InputProcessor
import com.example.facex.data.local.ml.liteRT.ModelValidatorFactory
import com.example.facex.data.local.ml.liteRT.OutputProcessor
import com.example.facex.data.local.ml.liteRT.TFLiteInterpreterWrapper
import com.example.facex.data.local.ml.liteRT.modelhandling.LiteRTModelConfig
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelType
import com.example.facex.data.toEntity
import com.example.facex.domain.entities.RecognitionConfig
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.logger.logWarning
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.MappedByteBuffer
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLRepositoryImpl @Inject constructor(
    private val modelConfigDao: ModelConfigDao,
    private val modelStorageManager: ModelStorageManager,
    private val modelValidatorFactory: ModelValidatorFactory,
    private val logger: Logger,
) : MLRepository {

    private val interpreterCache = ConcurrentHashMap<Long, TFLiteInterpreterWrapper<*, *>>()
    private val configCache = ConcurrentHashMap<Long, LiteRTModelConfig>()
    private val mutex = Mutex()


    override suspend fun getModelConfig(modelId: Long): Result<Pair<RecognitionConfig.LiteRT, MappedByteBuffer>> {
        return try {
            modelId.logDebug(logger) { "Getting model config for ID: $it" }

            val entity = modelConfigDao.getModelConfigById(modelId)
                ?.logDebug(logger) { "Loaded config entity: $it" } ?: return Result.failure(
                IllegalStateException("Model config not found").logWarning(logger) { "Model config not found for ID: $modelId" },
            )

            val mappedByteBuffer = modelStorageManager.getModelBuffer(entity.modelPath)

            val config = entity.toEntity()
            mutex.withLock {
                configCache[modelId] = config
            }

            Result.success(
                RecognitionConfig.LiteRT(
                    modelConfig = config
                ) to mappedByteBuffer
            ).logInfo(logger) { "Successfully loaded model config and file for ID: $modelId" }
        } catch (e: Exception) {
            logger.logError("Failed to get model config for ID: $modelId", e)
            Result.failure(e)
        }
    }

    override suspend fun <T, R> getInterpreter(
        modelId: Long,
        inputProcessor: InputProcessor<T>,
        outputProcessor: OutputProcessor<R>
    ): Result<TFLiteInterpreterWrapper<T, R>> {
        return try {
            modelId.logDebug(logger) { "Getting interpreter for model ID: $it" }

            // Check interpreter cache first (no lock needed for reading)
            interpreterCache[modelId]?.let { it->
                return Result.success(it)
                    .logDebug(logger) { "Cache hit for interpreter ID: $modelId" } as Result<TFLiteInterpreterWrapper<T, R>>
            }

            val (config, modelFile) = getModelConfig(modelId).getOrThrow()

            // More lenient validation approach
//            val modelValidator = modelValidatorFactory.getValidator(
//                ModelType.EMBEDDING_GENERATION, config.modelConfig
//            )
//            try {
//                if (!modelValidator(modelFile)) {
//                    return Result.failure(
//                        IllegalStateException(
//                            "Model validation failed for ID: $modelId".logWarning(logger)
//                        )
//                    )
//                }
//            } catch (e: Exception) {
//                return Result.failure(
//                    IllegalStateException(
//                        "Model validation failed but continuing: ${e.message}".logWarning(logger)
//                    )
//                )
//
//            }

            // Create and cache interpreter
            val interpreter = TFLiteInterpreterWrapper(
                config = config.modelConfig,
                modelMappedByteBuffer = modelFile,
                inputProcessor = inputProcessor,
                outputProcessor = outputProcessor,
                logger = logger
            )

            mutex.withLock {
                interpreterCache[modelId] = interpreter
            }

            Result.success(interpreter)
                .logInfo(logger) { "Created new interpreter for model ID: $modelId" }
        } catch (e: Exception) {
            logger.logError("Failed to get interpreter for model ID: $modelId", e)
            Result.failure(e)
        }
    }

    override suspend fun releaseInterpreter(modelId: Long): Unit = mutex.withLock {
        modelId.logDebug(logger) { "Releasing interpreter for model ID: $it" }
        interpreterCache.remove(modelId)?.close()
        configCache.remove(modelId)
    }

    override suspend fun releaseAllInterpreters() = mutex.withLock {
        "Releasing all interpreters and cached data".logInfo(logger) { it }
        interpreterCache.forEach { (_, interpreter) -> interpreter.close() }
        interpreterCache.clear()
        configCache.clear()
    }

    override suspend fun getDefaultModelConfigByType(modelType: ModelType): ModelConfigDTO? =
        modelConfigDao.getDefaultModelConfigByType(modelType.name)
}
