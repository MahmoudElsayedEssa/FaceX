package com.example.facex.data.local.ml.liteRT.modelhandling

data class ModelOutputConfig(
    val dataType: DataType,
    val embeddingSize: Int,
    val scale: Float = 1.0f,
    val zeroPoint: Int = 0,
)

enum class DataType(
    val byteSize: Int,
) {
    FLOAT32(4), INT8(1), UINT8(1);

    fun toTFLiteType(): org.tensorflow.lite.DataType = when (this) {
        FLOAT32 -> org.tensorflow.lite.DataType.FLOAT32
        INT8 -> org.tensorflow.lite.DataType.INT8
        UINT8 -> org.tensorflow.lite.DataType.UINT8
    }
}
