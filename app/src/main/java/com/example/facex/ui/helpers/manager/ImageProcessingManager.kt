package com.example.facex.ui.helpers.manager

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.ImageInput
import com.example.facex.ui.helpers.models.ImageOutputType
import com.example.facex.ui.helpers.operations.ImageOperation
import com.example.facex.ui.helpers.processors.formats.ImageFormatProcessor
import com.example.facex.ui.helpers.processors.formats.yuv420888.YUV420888ProcessorImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class ImageProcessingManager(
    private val operations: List<ImageOperation>,
    private val formatProcessors: Map<Int, ImageFormatProcessor> = mapOf(
        ImageFormat.YUV_420_888 to YUV420888ProcessorImage,
    )
) {
    suspend fun process(image: ImageProxy, outputType: ImageOutputType): ImageInput {
        val format = image.format
        val formatProcessor = formatProcessor(format)
        val processedImage = formatProcessor.process(image, outputType)
        return applyOperations(processedImage)
    }

    suspend fun processParallel(
        image: ImageProxy, outputType: ImageOutputType
    ): ImageInput = coroutineScope {
        val format = image.format
        val formatProcessor = formatProcessor(format)
        val processedImage = formatProcessor.process(image, outputType)

        val deferredResults = operations.map { operation ->
            async {
                val result = operation.process(processedImage)
                logOperation(operation, result)
                result
            }
        }

        deferredResults.awaitAll().last()
    }

    private suspend fun applyOperations(initialImage: ImageInput): ImageInput {
        return operations.fold(initialImage) { acc, operation ->
            operation.process(acc).also { logOperation(operation, it) }
        }
    }

    private fun logOperation(operation: ImageOperation, processedImage: ImageInput) {
        Log.d(
            TAG, "Applied Operation: ${operation::class.simpleName}, Result: $processedImage"
        )
    }

    private fun formatProcessor(format: Int): ImageFormatProcessor {
        return formatProcessors[format] ?: error("Unsupported image format: $format")
    }

    companion object {
        const val TAG = "ImageProcessingManager"
    }
}