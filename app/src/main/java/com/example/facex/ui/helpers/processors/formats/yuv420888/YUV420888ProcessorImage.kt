package com.example.facex.ui.helpers.processors.formats.yuv420888

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import com.example.facex.domain.entities.ImageInput
import com.example.facex.ui.helpers.models.ImageOutputType
import com.example.facex.ui.helpers.processors.conversion.ImageFormatConversionFactory
import com.example.facex.ui.helpers.processors.formats.ImageFormatProcessor

//object YUV420888Processor : FormatProcessorStrategy {
//    override suspend fun processImage(image: ImageProxy, outputType: ImageOutputType): ImageInput {
//        require(image.format == ImageFormat.YUV_420_888) {
//            "Unsupported format for YUV420888Processor: ${image.format}"
//        }
//
//        return when (outputType) {
//            is ImageOutputType.Bitmap -> toBitmap(image, outputType.config)
//            is ImageOutputType.ByteArray -> toByteArray(image, outputType.format)
//            is ImageOutputType.ByteBuffer -> toByteBuffer(image, outputType.format)
//        }
//    }
//
//    private suspend fun toBitmap(image: ImageProxy, config: Bitmap.Config): ImageInput.FromBitmap {
//        val bitmap = image.toBitmap()
//        return ImageInput.FromBitmap(bitmap)
//    }
//
//    private suspend fun toByteArray(image: ImageProxy): ImageInput.FromByteArray {
//        val yuvBytes = imageToByteArray(image)
//        return ImageInput.FromByteArray(yuvBytes, image.width, image.height, image.format)
//    }
//
//    private suspend fun toByteBuffer(image: ImageProxy): ImageInput.FromByteBuffer {
//        val yPlane = image.planes[0].buffer
//        val uPlane = image.planes[1].buffer
//        val vPlane = image.planes[2].buffer
//
//        val ySize = image.width * image.height
//        val uvSize = image.width * image.height / 2
//
//        val buffer = ByteBuffer.allocate(ySize + uvSize)
//
//        buffer.put(yPlane)
//
//        val uRowStride = image.planes[1].rowStride
//        val vRowStride = image.planes[2].rowStride
//        val pixelStride = image.planes[1].pixelStride // same for U and V
//
//        val avgHeight = image.height / 2
//        val avgWidth = image.width / 2
//        for (row in 0 until avgHeight) {
//            var uvIndex = row * uRowStride
//            for (col in 0 until avgWidth) {
//                buffer.put(uPlane.get(uvIndex)) // U data
//                buffer.put(vPlane.get(uvIndex)) // V data
//                uvIndex += pixelStride
//            }
//        }
//
//        // Rewind the buffer to the start
//        buffer.rewind()
//
//        return ImageInput.FromByteBuffer(buffer, image.width, image.height, image.format)
//    }
//
//    private suspend fun imageToByteArray(image: ImageProxy): ByteArray {
//        val yBuffer = image.planes[0].buffer
//        val uBuffer = image.planes[1].buffer
//        val vBuffer = image.planes[2].buffer
//
//        val ySize = yBuffer.remaining()
//        val uSize = uBuffer.remaining()
//        val vSize = vBuffer.remaining()
//
//        val nv21 = ByteArray(ySize + uSize + vSize)
//
//        yBuffer.get(nv21, 0, ySize)
//        vBuffer.get(nv21, ySize, vSize)
//        uBuffer.get(nv21, ySize + vSize, uSize)
//
//        return nv21
//    }
//}


//object YUV420888Processor : FormatProcessorStrategy {
//
//    override suspend fun processImage(image: ImageProxy, outputType: ImageOutputType): ImageInput {
//        val imageInput = ImageInput.FromImageProxy(image)
//
//        // Use the factory to get the appropriate conversion strategy
//        val conversionStrategy =
//            FormatConversionFactory.getConversionStrategy(image.format, getOutputFormat(outputType))
//
//        // Convert the image to the desired format using the strategy
//        return conversionStrategy.convert(imageInput)
//    }
//
//    // This function determines the target output format (NV21 for ByteBuffer, etc.)
//    private fun getOutputFormat(outputType: ImageOutputType): Int {
//        return when (outputType) {
//            is ImageOutputType.Bitmap -> ImageFormat.UNKNOWN // Bitmap is not tied to a specific format
//            is ImageOutputType.ByteArray -> outputType.format
//            is ImageOutputType.ByteBuffer -> outputType.format
//        }
//    }
//}


object YUV420888ProcessorImage : ImageFormatProcessor {
    override suspend fun process(image: ImageProxy, outputType: ImageOutputType): ImageInput {
        val imageInput = ImageInput.FromImageProxy(image)
        val outputFormat = when (outputType) {
            is ImageOutputType.Bitmap -> ImageFormat.UNKNOWN
            is ImageOutputType.ByteArray, is ImageOutputType.ByteBuffer -> outputType.format
        }

        val conversionStrategy = ImageFormatConversionFactory.getConversionStrategy(
            image.format, outputFormat
        )

        return conversionStrategy.convert(imageInput, outputType)
    }
}
