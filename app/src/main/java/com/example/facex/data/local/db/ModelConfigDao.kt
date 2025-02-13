package com.example.facex.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ModelConfigDao {
    @Query("SELECT * FROM model_configs WHERE modelType = :type")
    suspend fun getModelConfigsByType(type: String): List<ModelConfigDTO>

    @Query("SELECT * FROM model_configs WHERE id = :id")
    suspend fun getModelConfigById(id: Long): ModelConfigDTO?

    @Query("SELECT * FROM model_configs WHERE modelType = :type ORDER BY createdAt ASC LIMIT 1")
    suspend fun getDefaultModelConfigByType(type: String): ModelConfigDTO?

    @Insert
    suspend fun insertModelConfig(config: ModelConfigDTO): Long

    @Update
    suspend fun updateModelConfig(config: ModelConfigDTO)

    @Delete
    suspend fun deleteModelConfig(config: ModelConfigDTO)

    @Query("SELECT * FROM model_configs WHERE modelPath = :path")
    suspend fun getModelConfigByPath(path: String): ModelConfigDTO?
}
