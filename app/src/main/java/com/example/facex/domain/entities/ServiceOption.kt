package com.example.facex.domain.entities

data class ServiceOption(
    val id: Int,
    val name: String,
    val description: String,
    val serviceType: ProcessorType,
    val models: List<ModelOption>?,
    val isCurrent: Boolean = false,
)

data class ModelOption(
    val id: Int,
    val name: String,
    val path: String = "",
    val description: String,
    val threshold: Float = 0.5f,
    val isCurrent: Boolean = false,
    val modelAcceleration: ModelAcceleration = ModelAcceleration.GPU
)

enum class ModelAcceleration(val displayName: String) {
    CPU("CPU"), GPU("GPU"), NNAPI("NNAPI")
}


