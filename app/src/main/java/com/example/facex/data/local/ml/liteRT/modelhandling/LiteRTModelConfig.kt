package com.example.facex.data.local.ml.liteRT.modelhandling

import com.example.facex.data.local.ml.liteRT.DelegateType

data class LiteRTModelConfig(
    val modelPath: String = "ms1m_mobilenetv2_16.tflite",
    val numThreads: Int = 4,
    val delegate: DelegateType = DelegateType.CPU,
    val inputConfig: ModelInputConfig = ModelInputConfig(
        inputHeight = 112,
        inputWidth = 112,
        imageMean = 127.5f,
        imageStd = 127.5f,
    ),
    val outputConfig: ModelOutputConfig = ModelOutputConfig(
        dataType = DataType.FLOAT32,
        embeddingSize = 512,
        scale = 1.0f,
        zeroPoint = 0,
    ),
) {

    init {
        require(numThreads > 0) { "Number of threads must be greater than 0." }
        require(modelPath.isNotEmpty()) { "Model path cannot be empty." }
        require(inputConfig.inputHeight > 0 && inputConfig.inputWidth > 0) {
            "Invalid input dimensions"
        }
        require(inputConfig.imageStd != 0f) { "Image standardization cannot be zero" }
        require(outputConfig.embeddingSize > 0) { "Invalid embedding size" }
    }
}
