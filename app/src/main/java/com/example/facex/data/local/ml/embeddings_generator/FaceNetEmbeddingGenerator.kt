package com.example.facex.data.local.ml.embeddings_generator

import com.example.facex.data.local.ml.tensorflow.TFImageProcessor
import com.example.facex.data.local.ml.tensorflow.ModelHandler
import com.example.facex.data.local.ml.tensorflow.entity.ModelConfig
import com.example.facex.domain.entities.ImageInput
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer


class FaceNetEmbeddingGenerator(
    private val modelHandler: ModelHandler,
    private val imageProcessor: TFImageProcessor,
    private val modelConfig: ModelConfig
) : EmbeddingGenerator {

    private val outputBuffer: TensorBuffer = TensorBuffer.createFixedSize(
        modelConfig.outputShape,
        modelConfig.outputDataType
    )

    override suspend fun generateEmbedding(input: ImageInput): Result<ByteBuffer> {
        return runCatching {
            val processedBuffer = imageProcessor.process(input)
            modelHandler.runInference(processedBuffer, outputBuffer.buffer)
            outputBuffer.buffer
        }
    }

    override fun getEmbeddingAsFloatArray(): FloatArray {
        return when (modelConfig.outputDataType) {
            DataType.FLOAT32 -> outputBuffer.floatArray
            DataType.UINT8 -> outputBuffer.intArray.map { it.toFloat() / 255.0f }.toFloatArray()
            else -> throw IllegalStateException("Unsupported output data type: ${modelConfig.outputDataType}")
        }
    }

    override fun close() {
        // No need to close anything here as ModelHandler is managed externally
    }
}
