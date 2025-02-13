package com.example.facex.data.local.ml

import android.net.Uri
import java.nio.MappedByteBuffer

interface ModelStorageManager {
    suspend fun getModelBuffer(path: String): MappedByteBuffer
    suspend fun saveModel(uri: Uri, filename: String): Result<String>
    suspend fun deleteModel(path: String): Result<Unit>
}

