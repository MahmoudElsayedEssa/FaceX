package com.example.facex.data.local.ml.tensorflow

import android.content.Context
import com.example.facex.data.local.ml.tensorflow.delegation.DelegateHandler
import com.example.facex.data.local.ml.tensorflow.delegation.DelegateType
import com.example.facex.data.local.ml.tensorflow.delegation.TFLiteDelegateHandler
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


class TFLiteModelHandler(
    private val context: Context,
    private val delegateHandler: DelegateHandler
) : ModelHandler {
    private var interpreter: Interpreter? = null
    private var currentModelName: String = ""
    private var currentDelegateType: DelegateType = DelegateType.CPU

    override suspend fun loadModel(modelName: String): Result<Unit> =
        runCatching {
            val model = loadModelFile(modelName)
            val options = delegateHandler.createInterpreterOptions(currentDelegateType)
            interpreter?.close()
            interpreter = Interpreter(model, options)
            currentModelName = modelName
        }

    private fun loadModelFile(modelName: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override suspend fun runInference(input: ByteBuffer, output: ByteBuffer): Result<Unit> =
        runCatching {
            interpreter?.run(input, output)
                ?: error("Interpreter is not initialized")
        }

    override suspend fun changeDelegate(delegateType: DelegateType): Result<Unit> =
        runCatching {
            if (delegateType != currentDelegateType) {
                currentDelegateType = delegateType
                loadModel(currentModelName)
            }
        }

    override fun getInputTensorShape(): IntArray {
        requireNotNull(interpreter) { "Interpreter is not initialized" }
        return interpreter?.getInputTensor(0)?.shape()!!

    }


    override fun getOutputTensorShape(): IntArray {
        requireNotNull(interpreter) { "Interpreter is not initialized" }
        return interpreter?.getOutputTensor(0)?.shape()!!
    }

    override fun getInputDataType(): DataType {
        requireNotNull(interpreter) { "Interpreter is not initialized" }
        return interpreter?.getInputTensor(0)?.dataType()!!
    }

    override fun getOutputDataType(): DataType {
        requireNotNull(interpreter) { "Interpreter is not initialized" }
        return interpreter?.getOutputTensor(0)?.dataType()!!
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }
}
