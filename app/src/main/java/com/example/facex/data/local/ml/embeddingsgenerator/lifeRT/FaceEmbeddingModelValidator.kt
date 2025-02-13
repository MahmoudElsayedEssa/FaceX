package com.example.facex.data.local.ml.embeddingsgenerator.lifeRT

import com.example.facex.data.local.ml.liteRT.modelhandling.LiteRTModelConfig
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelInputConfig
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelOutputConfig
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelValidator
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.nio.MappedByteBuffer
import javax.inject.Inject

class FaceEmbeddingModelValidator @Inject constructor() : ModelValidator {

    override fun validate(modelBuffer: MappedByteBuffer, config: LiteRTModelConfig): Boolean {
        try {
            // 1. Basic buffer checks
            if (modelBuffer.capacity() < MIN_MODEL_SIZE || modelBuffer.capacity() > MAX_MODEL_SIZE) {
                return false
            }

            // 2. Debug the buffer content
            debugModelBuffer(modelBuffer)

            // 3. Validate model structure with interpreter
            return Interpreter(modelBuffer, Interpreter.Options()).use { interpreter ->
                validateModelStructure(interpreter, config)
            }
        } catch (e: Exception) {
            return false
        }
    }

    private fun debugModelBuffer(buffer: MappedByteBuffer) {
        val originalPosition = buffer.position()
        try {
            buffer.position(0)
            val header = ByteArray(16)
            buffer.get(header)
        } finally {
            buffer.position(originalPosition)  // Reset position
        }
    }

    private fun validateModelStructure(
        interpreter: Interpreter,
        config: LiteRTModelConfig
    ): Boolean {
        try {
            // 1. Validate input tensor
            val inputTensor = interpreter.getInputTensor(0)
            if (!validateInputTensor(inputTensor, config.inputConfig)) {
                return false
            }

            // 2. Validate output tensor
            val outputTensor = interpreter.getOutputTensor(0)
            return validateOutputTensor(outputTensor, config.outputConfig)
        } catch (e: Exception) {
            return false
        }
    }

    private fun validateInputTensor(
        tensor: Tensor,
        inputConfig: ModelInputConfig
    ): Boolean = try {
        val shape = tensor.shape()
        shape.size == 4 && // NHWC format
                shape[0] == 1 && // Batch size
                shape[1] == inputConfig.inputHeight &&
                shape[2] == inputConfig.inputWidth &&
                shape[3] == INPUT_CHANNELS && // RGB channels
                tensor.dataType() == DataType.FLOAT32
    } catch (e: Exception) {
        false
    }

    private fun validateOutputTensor(
        tensor: Tensor,
        outputConfig: ModelOutputConfig
    ): Boolean = try {
        val shape = tensor.shape()
        shape.size == 2 && // [batch_size, embedding_size]
                shape[0] == 1 && // Batch size
                shape[1] == outputConfig.embeddingSize &&
                tensor.dataType() == outputConfig.dataType.toTFLiteType()
    } catch (e: Exception) {
        false
    }

    companion object {
        private const val MIN_MODEL_SIZE = 1024 * 1024 // 1MB minimum
        private const val MAX_MODEL_SIZE = 100 * 1024 * 1024 // 100MB maximum
        private const val INPUT_CHANNELS = 3 // RGB input
    }
}