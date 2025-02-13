package com.example.facex.data.local.ml

import com.example.facex.data.local.ml.embeddingsgenerator.lifeRT.LiteRTEmbeddingGenerator
import com.example.facex.domain.entities.FaceDetectionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.logger.logWarning
import com.example.facex.domain.ml.EmbeddingGenerator
import com.example.facex.domain.ml.FaceDetector
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.ml.MlOperation
import com.example.facex.domain.ml.config.FaceDetectionService
import com.example.facex.domain.ml.config.FaceRecognitionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException

class FaceProcessorFacadeImpl private constructor(
    initialFaceDetector: FaceDetector,
    initialEmbeddingGenerator: EmbeddingGenerator,
    private val mlRepository: MLRepository,
    private val logger: Logger,
    private val detectorType: FaceDetectionService,
    private val recognitionType: FaceRecognitionService
) : FaceProcessorFacade {

    private val isReleased = AtomicBoolean(false)
    private val currentFaceDetector = AtomicReference(initialFaceDetector)
    private val currentEmbeddingGenerator = AtomicReference(initialEmbeddingGenerator)
    private val resourceManager = ResourceManager()

    init {
        logger.tag = this::class.simpleName.toString()
        "Initializing FaceProcessor".logInfo(logger)
        resourceManager.register(initialFaceDetector)
        resourceManager.register(initialEmbeddingGenerator)
    }

    override suspend fun detectFaces(
        image: Frame,
    ): Result<List<FaceDetectionResult>> {
        checkNotReleased()
        return try {
            val detector = currentFaceDetector.get()
            detector.detectFaces(image).logInfo(logger) { "Face detection completed successfully" }
        } catch (e: Exception) {
            handleException(e, "Face detection")
        }
    }

    override suspend fun generateEmbedding(
        input: Frame
    ): Result<FloatArray> {
        checkNotReleased()
        return try {
            val generator = currentEmbeddingGenerator.get()
            generator.generateEmbedding(input).logDebug(logger) { "Embedding generation completed" }
        } catch (e: Exception) {
            handleException(e, "Embedding generation")
        }
    }

    override suspend fun updateFaceDetector(newDetector: FaceDetector): Result<Unit> {
        checkNotReleased()
        return try {
            "Updating face detector".logInfo(logger)
            val oldDetector = currentFaceDetector.getAndSet(newDetector)
            resourceManager.register(newDetector)
            oldDetector.close()
            Result.success(Unit).logInfo(logger) { "Face detector updated successfully" }
        } catch (e: Exception) {
            logger.logError("Failed to update face detector", e)
            Result.failure(e)
        }
    }

    override suspend fun updateFaceRecognition(newDetector: EmbeddingGenerator): Result<Unit> {
        checkNotReleased()
        return try {
            "Updating face recognition".logInfo(logger)
            val oldDetector = currentEmbeddingGenerator.getAndSet(newDetector)
            resourceManager.register(newDetector)
            oldDetector.close()
            Result.success(Unit).logInfo(logger) { "Face detector updated successfully" }
        } catch (e: Exception) {
            logger.logError("Failed to update face detector", e)
            Result.failure(e)
        }

    }

    suspend fun updateEmbeddingGeneratorModel(modelId: Long): Result<Unit> {
        checkNotReleased()
        return try {
            val newGenerator = preloadEmbeddingGenerator(modelId).getOrThrow()
            val oldGenerator = currentEmbeddingGenerator.getAndSet(newGenerator)
            resourceManager.register(newGenerator)
            oldGenerator.close()
            Result.success(Unit).logInfo(logger) { "Embedding generator updated successfully" }
        } catch (e: Exception) {
            logger.logError("Failed to update embedding generator", e)
            Result.failure(e)
        }
    }

    private suspend fun preloadEmbeddingGenerator(modelId: Long): Result<EmbeddingGenerator> =
        withContext(Dispatchers.IO) {
            try {
                val newConfig = mlRepository.getModelConfig(modelId).getOrThrow().first
                val newGenerator = LiteRTEmbeddingGenerator(
                    mlRepository = mlRepository,
                    config = newConfig,
                    logger = logger,
                ).apply { configure(modelId) }
                Result.success(newGenerator)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun close() {
        if (isReleased.compareAndSet(false, true)) {
            try {
                "Releasing FaceProcessor resources".logDebug(logger) { it }
                resourceManager.closeAll()
                "FaceProcessor resources released successfully".logDebug(logger) { it }
            } catch (e: Exception) {
                logger.logError("Error during resource release", e)
                throw e
            }
        }
    }

    override suspend fun getCurrentDetectorType() = Result.success(detectorType)
    override suspend fun getCurrentRecognitionType() = Result.success(recognitionType)

    private fun checkNotReleased() {
        if (isReleased.get()) {
            throw IllegalStateException("FaceProcessor has been released").logWarning(logger) { "Attempted to use released FaceProcessor" }
        }
    }

    private fun handleException(e: Exception, operation: String): Result<Nothing> {
        return when (e) {
            is IllegalStateException -> {
                logger.logError("$operation in invalid state", e)
                Result.failure(e)
            }

            is CancellationException -> {
                "$operation cancelled".logWarning(logger)
                throw e
            }

            else -> {
                logger.logError("Unexpected error during $operation", e)
                Result.failure(e)
            }
        }
    }

    class ResourceManager {
        private val resources = mutableListOf<MlOperation>()

        fun register(resource: MlOperation) {
            synchronized(resources) {
                resources.add(resource)
            }
        }

        suspend fun closeAll() {
            resources.forEach { it.close() }
            synchronized(resources) {
                resources.clear()
            }
        }
    }

    class Builder(
        private val logger: Logger,
        private val mlRepository: MLRepository,
        private val detectorType: FaceDetectionService,
        private val recognitionType: FaceRecognitionService,
    ) {
        private var faceDetector: FaceDetector? = null
        private var embeddingGenerator: EmbeddingGenerator? = null

        fun setFaceDetector(detector: FaceDetector) = apply {
            this.faceDetector = detector
        }

        fun setEmbeddingGenerator(generator: EmbeddingGenerator) = apply {
            this.embeddingGenerator = generator
        }

        fun build(): FaceProcessorFacade {
            val detector = faceDetector ?: error("FaceDetector must be provided")
            val generator = embeddingGenerator ?: error("EmbeddingGenerator must be provided")

            "Building FaceProcessor".logInfo(logger) { it }
            return FaceProcessorFacadeImpl(
                detector, generator, mlRepository, logger, detectorType, recognitionType
            )
        }
    }
}



