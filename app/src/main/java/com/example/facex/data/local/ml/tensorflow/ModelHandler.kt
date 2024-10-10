package com.example.facex.data.local.ml.tensorflow

import com.example.facex.data.local.ml.tensorflow.delegation.DelegateType
import org.tensorflow.lite.DataType
import java.nio.ByteBuffer

interface ModelHandler : AutoCloseable {
    suspend fun runInference(input: ByteBuffer, output: ByteBuffer): Result<Unit>
    fun getInputTensorShape(): IntArray
    fun getOutputTensorShape(): IntArray
    fun getInputDataType(): DataType
    fun getOutputDataType(): DataType
    suspend fun loadModel(modelName: String): Result<Unit>
    suspend fun changeDelegate(delegateType: DelegateType): Result<Unit>
}
