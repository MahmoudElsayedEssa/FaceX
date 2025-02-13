package com.example.facex.data.local.ml.embeddingsgenerator.lifeRT

import com.example.facex.data.local.ml.MLRepository
import com.example.facex.data.local.ml.liteRT.TFLiteInterpreterWrapper
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelType
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.entities.RecognitionConfig
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.logger.logWarning
import com.example.facex.domain.ml.EmbeddingGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiteRTEmbeddingGenerator @Inject constructor(
    private val mlRepository: MLRepository,
    private val config: RecognitionConfig.LiteRT,
    private val logger: Logger,
) : EmbeddingGenerator {
    private var currentInterpreter: TFLiteInterpreterWrapper<Frame, FloatArray>? = null
    private var currentModelId: Long? = null
    private val mutex = Mutex()


    private val inputProcessor by lazy { FaceEmbeddingInputProcessor(config.modelConfig.inputConfig) }
    private val outputProcessor by lazy { EmbeddingOutputProcessor(config.modelConfig.outputConfig) }

    init {
        logger.tag = this::class.simpleName.toString()
        CoroutineScope(Dispatchers.IO).launch {
            initializeDefaultModel()
        }
    }

    private suspend fun initializeDefaultModel() {
        mutex.withLock {
            try {
                val defaultConfig =
                    mlRepository.getDefaultModelConfigByType(ModelType.EMBEDDING_GENERATION)
                        ?: throw IllegalStateException("No default embedding model found")

                currentModelId = defaultConfig.id
                configure(defaultConfig.id)
                "Initialized with default embedding model ID: ${defaultConfig.id}".logDebug(logger)
            } catch (e: Exception) {
                logger.logError("Failed to initialize default embedding model", e)
            }
        }
    }

    override suspend fun generateEmbedding(
        input: Frame,
    ): Result<FloatArray> {
        input.logDebug(logger) { "Generating embedding" }


        val interpreter = currentInterpreter ?: createNewInterpreter(currentModelId)

        return interpreter.predict(input).logInfo(logger) {
            "Generated embedding successfully , embedding : $it" +
                    " with config: $config"
        }
    }

    suspend fun configure(modelId: Long) = mutex.withLock {
        modelId.logInfo(logger) { "Configuring generator with model ID: $it" }

        val oldModelId = currentModelId
        val newInterpreter = createNewInterpreter(oldModelId)

        currentModelId = modelId
        currentInterpreter = newInterpreter

        oldModelId?.let { mlRepository.releaseInterpreter(it) }
    }

    private suspend fun createNewInterpreter(modelId: Long?): TFLiteInterpreterWrapper<Frame, FloatArray> {
        requireNotNull(modelId) {
            IllegalStateException("No model configured")
                .logWarning(logger) { "Attempted to generate embedding without configured model" }
        }

        val newInterpreter = mlRepository.getInterpreter(
            modelId = currentModelId!!,
            inputProcessor = inputProcessor,
            outputProcessor = outputProcessor,
        ).getOrThrow()
        return newInterpreter
    }

    override suspend fun close() {
        "Closing embedding generator".logInfo(logger) { it }
        currentModelId?.let { mlRepository.releaseInterpreter(it) }
        currentModelId = null
    }
}
