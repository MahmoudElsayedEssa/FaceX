package com.example.facex.data

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.facex.data.local.db.ModelConfigDTO
import com.example.facex.data.local.ml.liteRT.DelegateType
import com.example.facex.data.local.ml.liteRT.modelhandling.DataType
import com.example.facex.data.local.ml.liteRT.modelhandling.LiteRTModelConfig
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelInputConfig
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelOutputConfig
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.ImageFormat
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun Rect.toOpenCVRect(): org.opencv.core.Rect = org.opencv.core.Rect(left, top, width(), height())

fun Mat.cropAndConvert(boundingBox: Rect): Bitmap {
    val validBoundingBox =
        Rect(
            boundingBox.left.coerceIn(0, this.width()),
            boundingBox.top.coerceIn(0, this.height()),
            boundingBox.right.coerceIn(0, this.width()),
            boundingBox.bottom.coerceIn(0, this.height()),
        )

    val croppedMat = Mat(this, validBoundingBox.toOpenCVRect())

    val croppedBitmap =
        Bitmap.createBitmap(
            validBoundingBox.width(),
            validBoundingBox.height(),
            Bitmap.Config.RGB_565,
        )
    Utils.matToBitmap(croppedMat, croppedBitmap)
    croppedMat.release()
    this.release()

    return croppedBitmap
}

fun Bitmap.toGrayScale(): Bitmap {
    val mat = Mat()
    Utils.bitmapToMat(this, mat)

    val grayMat = Mat()
    Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY)

    val grayscaleBitmap = Bitmap.createBitmap(grayMat.cols(), grayMat.rows(), Bitmap.Config.RGB_565)

    Utils.matToBitmap(grayMat, grayscaleBitmap)

    mat.release()
    grayMat.release()

    return grayscaleBitmap
}

fun Bitmap.scaleBitmap(scaleFactor: Float): Bitmap {
    // Convert Bitmap to Mat
    val mat = Mat()
    Utils.bitmapToMat(this, mat)

    // Create a new Mat for the scaled output
    val scaledMat = Mat()

    // Resize the Mat using OpenCV
    Imgproc.resize(
        mat,
        scaledMat,
        org.opencv.core.Size(
            (mat.cols() * scaleFactor).toDouble(),
            (mat.rows() * scaleFactor).toDouble(),
        ),
    )

    // Create a Bitmap from the scaled Mat
    val scaledBitmap =
        Bitmap.createBitmap(scaledMat.cols(), scaledMat.rows(), Bitmap.Config.RGB_565)
    Utils.matToBitmap(scaledMat, scaledBitmap)

    // Release Mats to prevent memory leaks
    mat.release()
    scaledMat.release()

    return scaledBitmap
}

// private fun ByteBuffer.cropByteBuffer(sourceWidth: Int, sourceHeight: Int, rect: Rect): ByteBuffer {
//    val croppedWidth = rect.width()
//    val croppedHeight = rect.height()
//    val croppedBuffer = ByteBuffer.allocateDirect(croppedWidth * croppedHeight)
//
//    for (y in rect.top until rect.bottom) {
//        val sourceOffset = y * sourceWidth + rect.left
//        val destOffset = (y - rect.top) * croppedWidth
//        position(sourceOffset)
//        get(croppedBuffer.array(), destOffset, croppedWidth)
//    }
//
//    croppedBuffer.rewind()
//    return croppedBuffer
// }

fun ByteBuffer.cropByteBuffer(
    sourceWidth: Int,
    rect: Rect,
): ByteBuffer {
    val croppedWidth = rect.width()
    val croppedHeight = rect.height()

    // Create a new direct ByteBuffer for the cropped area
    val croppedBuffer = ByteBuffer.allocateDirect(croppedWidth * croppedHeight)

    // Loop through each row in the specified rectangle
    for (y in rect.top until rect.bottom) {
        val sourceOffset = y * sourceWidth + rect.left
        val destOffset = (y - rect.top) * croppedWidth

        // Use the ByteBuffer's methods to read and write directly
        this.position(sourceOffset)
        // Read the row from the source buffer and write it to the cropped buffer
        this.get(croppedBuffer.array(), destOffset, croppedWidth)
    }

    croppedBuffer.rewind()
    return croppedBuffer
}

fun ByteBuffer.cropByteBufferNV21(
    cropRect: Rect,
    originalWidth: Int,
): ByteBuffer? {
    // Check if the crop rectangle is valid
    if (cropRect.left < 0 || cropRect.top < 0 || cropRect.right > originalWidth || cropRect.bottom > capacity() / originalWidth) {
        return null // Return null for invalid crop rectangle
    }

    // Calculate cropped width and height
    val croppedWidth = cropRect.width()
    val croppedHeight = cropRect.height()

    // Allocate a new ByteBuffer for the cropped data
    val croppedBuffer = ByteBuffer.allocate(croppedWidth * croppedHeight)

    // Calculate the row stride
    val stride = originalWidth // Assuming the stride equals the image width for the Y plane

    // Copy data from the original ByteBuffer into the cropped ByteBuffer
    for (row in 0 until croppedHeight) {
        val sourceRow = cropRect.top + row // Calculate the source row
        val sourceOffset = sourceRow * stride + cropRect.left // Calculate the source offset

        // Set the position in the original buffer to the start of the row
        position(sourceOffset)
        // Read the data into the croppedBuffer
        get(croppedBuffer.array(), row * croppedWidth, croppedWidth)
    }

    // The position of croppedBuffer is already set to 0, so no need to rewind
    return croppedBuffer // Return the cropped ByteBuffer
}

fun ByteBuffer.toByteArray(): ByteArray {
    val byteArray = ByteArray(remaining())
    val oldPosition = position()
    get(byteArray)
    position(oldPosition)
    return byteArray
}

fun ByteArray.toByteBuffer(): ByteBuffer = ByteBuffer.wrap(this)

fun FloatArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(4 * size)
    buffer.order(ByteOrder.nativeOrder())
    buffer.asFloatBuffer().put(this)
    return buffer.array()}

fun ByteArray.toFloatArray(): FloatArray {
    val buffer = ByteBuffer.wrap(this)
    buffer.order(ByteOrder.nativeOrder())
    val floatBuffer = buffer.asFloatBuffer()
    val result = FloatArray(floatBuffer.remaining())
    floatBuffer.get(result)
    return result
}

fun Embedding.toByteArray() = data.toByteArray()

fun ModelConfigDTO.toEntity(): LiteRTModelConfig =
    LiteRTModelConfig(
        modelPath = modelPath,
        numThreads = numThreads,
        delegate = DelegateType.valueOf(delegate),
        inputConfig =
            ModelInputConfig(
                inputHeight = inputHeight,
                inputWidth = inputWidth,
                imageMean = imageMean,
                imageStd = imageStd,
            ),
        outputConfig =
            ModelOutputConfig(
                dataType = DataType.valueOf(dataType),
                embeddingSize = embeddingSize,
                scale = scale,
                zeroPoint = zeroPoint,
            ),
    )

fun modelConfigtoDTO(
    name: String,
    description: String,
    modelType: String,
    config: LiteRTModelConfig,
): ModelConfigDTO =
    ModelConfigDTO(
        name = name,
        description = description,
        modelType = modelType,
        modelPath = config.modelPath,
        numThreads = config.numThreads,
        delegate = config.delegate.name,
        inputHeight = config.inputConfig.inputHeight,
        inputWidth = config.inputConfig.inputWidth,
        imageMean = config.inputConfig.imageMean,
        imageStd = config.inputConfig.imageStd,
        dataType = config.outputConfig.dataType.name,
        embeddingSize = config.outputConfig.embeddingSize,
        scale = config.outputConfig.scale,
        zeroPoint = config.outputConfig.zeroPoint,
    )



fun ImageFormat.toImageFormat(): Int = when (this) {
    ImageFormat.RGBA8888 -> android.graphics.ImageFormat.FLEX_RGBA_8888
    ImageFormat.NV21 -> android.graphics.ImageFormat.NV21
    ImageFormat.YUV420888 -> android.graphics.ImageFormat.YUV_420_888
}
