package com.example.facex.data.local.ml.tensorflow.entity

import org.tensorflow.lite.DataType

data class ModelInputImageConfig(
    val inputWidth: Int,
    val inputHeight: Int,
    val imageMean: Float,
    val imageStd: Float,
    val dataType: DataType
)
