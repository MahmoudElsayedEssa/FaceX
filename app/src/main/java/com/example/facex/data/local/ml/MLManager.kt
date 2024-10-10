package com.example.facex.data.local.ml

import android.util.Log
import android.util.LruCache
import com.example.facex.data.local.ml.detection.FaceDetector
import com.example.facex.data.local.ml.entity.MLConfig
import com.example.facex.data.local.ml.entity.MLError
import com.example.facex.data.local.ml.entity.SimpleFace
import com.example.facex.data.local.ml.embeddings_generator.EmbeddingGenerator
import com.example.facex.data.local.ml.embeddings_generator.FaceNetEmbeddingGenerator
import com.example.facex.data.local.ml.tensorflow.ModelHandler
import com.example.facex.data.local.ml.tensorflow.TFImageProcessor
import com.example.facex.data.local.ml.tensorflow.delegation.DelegateHandler
import com.example.facex.data.local.ml.tensorflow.delegation.DelegateType
import com.example.facex.data.local.ml.tensorflow.entity.ModelConfig
import com.example.facex.data.local.ml.tensorflow.entity.ModelInputImageConfig
import com.example.facex.domain.entities.ImageInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLManager @Inject constructor(
    private val modelHandler: ModelHandler,
    delegateHandler: DelegateHandler,
    private val faceDetector: FaceDetector
) : AutoCloseable, MLOperations {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var currentDelegate: DelegateType = delegateHandler.selectBestDelegate()
    private var embeddingGenerator: EmbeddingGenerator? = null
    private var currentModelName: String = MLConfig.DEFAULT_MODEL_NAME

    private val embeddingCache = LruCache<String, ByteBuffer>(MLConfig.CACHE_SIZE)
    private lateinit var modelConfig: ModelConfig
    private lateinit var imageProcessor: TFImageProcessor

    init {
        scope.launch {
            initializeModel()
        }
    }

    private suspend fun initializeModel() {
        mutex.withLock {
            modelHandler.loadModel(MLConfig.DEFAULT_MODEL_NAME).getOrThrow()

            modelConfig = ModelConfig(
                inputShape = modelHandler.getInputTensorShape(),
                outputShape = modelHandler.getOutputTensorShape(),
                inputDataType = modelHandler.getInputDataType(),
                outputDataType = modelHandler.getOutputDataType()
            )

            imageProcessor = TFImageProcessor(
                ModelInputImageConfig(
                    inputHeight = modelConfig.inputShape[1],
                    inputWidth = modelConfig.inputShape[2],
                    imageMean = 127.5f,
                    imageStd = 127.5f,
                    dataType = modelConfig.inputDataType
                )
            )

            embeddingGenerator =
                FaceNetEmbeddingGenerator(modelHandler, imageProcessor, modelConfig)
        }
    }

    private suspend fun loadModel(modelName: String): Result<Unit> = mutex.withLock {
        return runCatching {
            modelHandler.loadModel(modelName).getOrThrow()
            currentModelName = modelName

            modelConfig = ModelConfig(
                inputShape = modelHandler.getInputTensorShape(),
                outputShape = modelHandler.getOutputTensorShape(),
                inputDataType = modelHandler.getInputDataType(),
                outputDataType = modelHandler.getOutputDataType()
            )

            imageProcessor = TFImageProcessor(
                ModelInputImageConfig(
                    inputHeight = modelConfig.inputShape[1],
                    inputWidth = modelConfig.inputShape[2],
                    imageMean = 127.5f,
                    imageStd = 127.5f,
                    dataType = modelConfig.inputDataType
                )
            )

            recreateEmbeddingGenerator()
        }.onFailure { error ->
            Log.e(TAG, "Failed to load model: $modelName", error)
            throw MLError.ModelLoadError(error)
        }
    }

    private suspend fun recreateEmbeddingGenerator() = mutex.withLock {
        embeddingGenerator?.close()
        embeddingGenerator = FaceNetEmbeddingGenerator(modelHandler, imageProcessor, modelConfig)
    }

    suspend fun changeDelegate(delegateType: DelegateType): Result<Unit> = mutex.withLock {
        return runCatching {
            modelHandler.changeDelegate(delegateType).getOrThrow()
            currentDelegate = delegateType
            recreateEmbeddingGenerator()
        }.onFailure { error ->
            Log.e(TAG, "Failed to change delegate to: $delegateType", error)
            throw MLError.DelegateChangeError(error)
        }
    }

    suspend fun changeFaceRecognizer(generator: EmbeddingGenerator) = mutex.withLock {
        if (embeddingGenerator != generator) {
            embeddingGenerator?.close()
            embeddingGenerator = generator
        }
    }

    suspend fun changeModel(modelName: String): Result<Unit> = loadModel(modelName)


    override suspend fun detectFaces(
        image: ImageInput, rotationDegrees: Int
    ): Result<List<SimpleFace>> = mutex.withLock {
        return runCatching {
            when (image) {
                is ImageInput.FromByteBuffer -> faceDetector?.detectFaces(
                    buffer = image.buffer,
                    width = image.width,
                    height = image.height,
                    format = image.format,
                    rotationDegrees = rotationDegrees
                )

                is ImageInput.FromByteArray -> faceDetector?.detectFaces(
                    array = image.array,
                    width = image.width,
                    height = image.height,
                    format = image.format,
                    rotationDegrees = rotationDegrees
                )

                is ImageInput.FromBitmap -> faceDetector?.detectFaces(image.bitmap, rotationDegrees)
                is ImageInput.FromImageProxy -> TODO()
            }?.getOrThrow() ?: emptyList()
        }.onFailure { error ->
            Log.e(TAG, "Face detection failed", error)
            if (error is MLError.InferenceError) {
                fallbackToCPUDelegate()
            }
        }
    }

    override suspend fun generateEmbedding(face: ImageInput): Result<ByteBuffer> = mutex.withLock {
        val cacheKey = when (face) {
            is ImageInput.FromByteBuffer -> face.buffer.hashCode().toString()
            is ImageInput.FromBitmap -> face.bitmap.hashCode().toString()
            is ImageInput.FromByteArray -> face.array.hashCode().toString()
            is ImageInput.FromImageProxy -> TODO()
        }
        embeddingCache.get(cacheKey)?.let { return Result.success(it) }

        return runCatching {
            val embedding = embeddingGenerator?.generateEmbedding(face)?.getOrThrow()
                ?: throw MLError.EmbeddingGenerationError(IllegalStateException("EmbeddingGenerator is null"))
            embeddingCache.put(cacheKey, embedding)
            embedding
        }.onFailure { error ->
            Log.e(TAG, "Embedding generation failed", error)
            if (error is MLError.InferenceError) {
                fallbackToCPUDelegate()
            }
        }
    }


    private suspend fun fallbackToCPUDelegate() {
        Log.w(TAG, "Falling back to CPU delegate")
        changeDelegate(DelegateType.CPU)
    }


    override fun close() {
        scope.cancel()
        faceDetector.close()
        embeddingGenerator?.close()
        modelHandler.close()
    }

    companion object {
        private const val TAG = "MLManager"
    }
}