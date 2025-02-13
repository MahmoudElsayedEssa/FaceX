package com.example.facex.data.local.ml.liteRT.modelhandling

import android.content.Context
import android.net.Uri
import com.example.facex.data.local.db.ModelConfigDTO
import com.example.facex.data.local.db.ModelConfigDao
import com.example.facex.data.local.ml.ModelStorageManager
import com.example.facex.data.local.ml.liteRT.DelegateType
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultModelsInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelConfigDao: ModelConfigDao,
    private val modelStorageManager: ModelStorageManager,
    private val logger: Logger,
) {
    private val modelDirectory by lazy {
        File(context.filesDir, "models").apply {
            if (!exists()) mkdirs()
        }
    }

    private val defaultEmbeddingConfig by lazy {
        ModelConfigDTO(
            name = "Default Face Embedding",
            description = "MobileNetV2 model for face embedding generation",
            modelType = ModelType.EMBEDDING_GENERATION.name,
            numThreads = 4,
            delegate = DelegateType.GPU.name,
            inputHeight = 112,
            inputWidth = 112,
            imageMean = 127.5f,
            imageStd = 127.5f,
            dataType = DataType.FLOAT32.name,
            embeddingSize = 512,
            scale = 1.0f,
            zeroPoint = 0,
            modelPath = "" // Will be set after saving
        )
    }

    private val mutex = Mutex()
    private val isInitialized = AtomicBoolean(false)

    suspend fun initializeDefaultModels() {
        if (isInitialized.get()) return

        try {
            val existingModels =
                modelConfigDao.getModelConfigsByType(ModelType.EMBEDDING_GENERATION.name)
            if (existingModels.isNotEmpty()) {
                isInitialized.set(true)
                "Default models already initialized".logDebug(logger)
                return
            }

            mutex.withLock {
                if (isInitialized.get()) return@withLock

                initializeEmbeddingModel()
                isInitialized.set(true)
            }
        } catch (e: Exception) {
            logger.logError("Failed to initialize default models", e)
            throw e
        }
    }

    private suspend fun initializeEmbeddingModel() {
        val assetPath = "ml/ms1m_mobilenetv2_16.tflite"
        val filename = "ms1m_mobilenetv2_16.tflite"

        try {
            // Create Uri from asset
            val assetUri =
                Uri.Builder().scheme("file").appendPath("android_asset").appendPath(assetPath)
                    .build()

            // Save model from assets to app directory
            val modelPath = modelStorageManager.saveModel(
                uri = assetUri, filename = filename
            ).getOrThrow()

            // Save config with the new path
            val config = defaultEmbeddingConfig.copy(modelPath = modelPath)
            modelConfigDao.insertModelConfig(config).also { id ->
                "Default models initialized successfully. Embedding ID: $id".logDebug(logger)
            }
        } catch (e: Exception) {
            logger.logError("Failed to initialize embedding model", e)
            throw e
        }
    }
}