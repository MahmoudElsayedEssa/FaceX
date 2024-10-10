package com.example.facex.data.local.ml.tensorflow.entity

import org.tensorflow.lite.DataType


data class ModelConfig(
    val inputShape: IntArray,
    val outputShape: IntArray,
    val inputDataType: DataType,
    val outputDataType: DataType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelConfig

        if (!inputShape.contentEquals(other.inputShape)) return false
        if (!outputShape.contentEquals(other.outputShape)) return false
        if (inputDataType != other.inputDataType) return false
        if (outputDataType != other.outputDataType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputShape.contentHashCode()
        result = 31 * result + outputShape.contentHashCode()
        result = 31 * result + inputDataType.hashCode()
        result = 31 * result + outputDataType.hashCode()
        return result
    }
}