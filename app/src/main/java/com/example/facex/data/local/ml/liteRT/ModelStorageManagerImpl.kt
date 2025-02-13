package com.example.facex.data.local.ml.liteRT

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.facex.data.local.ml.ModelStorageManager
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.logger.logWarning
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ModelStorageManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context, private val logger: Logger
) : ModelStorageManager {
    private val modelDirectory = File(context.filesDir, "models")
    private val mutex = Mutex()

    init {
        logger.tag = this::class.simpleName.toString()
        modelDirectory.mkdirs().logDebug(logger) { "Created models directory: $modelDirectory" }
    }

    override suspend fun getModelBuffer(path: String): MappedByteBuffer =
        withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (!file.exists()) {
                    // If file doesn't exist in app directory, try to load from assets
                    val assetName = file.name
                    val modelFile = File(context.filesDir, "models/${assetName}")
                    modelFile.parentFile?.mkdirs()

                    // Copy from assets
                    context.assets.open(assetName).use { input ->
                        FileOutputStream(modelFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Map the copied file
                    FileInputStream(modelFile).channel.use { channel ->
                        channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    }
                } else {
                    // Map existing file
                    FileInputStream(file).channel.use { channel ->
                        channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    }
                }
            } catch (e: Exception) {
                logger.logError("Failed to load model buffer", e)
                throw e
            }
        }

    override suspend fun saveModel(
        uri: Uri,
        filename: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(modelDirectory, filename)

            when (uri.scheme) {
                "file" -> {
                    if (uri.pathSegments.firstOrNull() == "android_asset") {
                        // Load from assets
                        val assetPath = uri.pathSegments.drop(1).joinToString("/")
                        context.assets.open(assetPath).use { input ->
                            FileOutputStream(modelFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } else {
                        // Load from file
                        FileInputStream(uri.path!!).use { input ->
                            FileOutputStream(modelFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }

                ContentResolver.SCHEME_CONTENT -> {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(modelFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: throw IOException("Could not open input stream for URI: $uri")
                }

                else -> throw IllegalArgumentException("Unsupported URI scheme: ${uri.scheme}")
            }

            Result.success(modelFile.absolutePath)
                .logInfo(logger) { "Model saved successfully: $filename" }
        } catch (e: Exception) {
            logger.logError("Failed to save model", e)
            Result.failure(IOException("Failed to save model", e))
        }
    }

    override suspend fun deleteModel(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val file = File(path)
                if (!file.exists()) {
                    return@withLock Result.failure(IOException("File does not exist: $path"))
                }

                if (file.delete()) {
                    Result.success(Unit).logInfo(logger) { "Model deleted successfully: $path" }
                } else {
                    Result.failure(IOException("Failed to delete file: $path").logWarning(
                        logger
                    ) { "Failed to delete model: $path" })
                }
            } catch (e: Exception) {
                logger.logError("Error deleting model: $path", e)
                Result.failure(e)
            }
        }
    }
}