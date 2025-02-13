package com.example.facex.data.local.ml.liteRT.modelhandling

data class ModelInputConfig(
    val inputHeight: Int,
    val inputWidth: Int,
    val imageMean: Float,
    val imageStd: Float,
)
