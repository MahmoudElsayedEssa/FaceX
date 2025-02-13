package com.example.facex.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "model_configs")
data class ModelConfigDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val modelPath: String,
    val modelType: String,
    val createdAt: Long = System.currentTimeMillis(),
    val numThreads: Int,
    val delegate: String,
    // Input config
    val inputHeight: Int,
    val inputWidth: Int,
    val imageMean: Float,
    val imageStd: Float,
    // Output config
    val dataType: String,
    val embeddingSize: Int,
    val scale: Float,
    val zeroPoint: Int,
)
