package com.example.facex.data

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val FLOAT_SIZE_BYTES = 4
fun ByteBuffer.toFloatArray(): FloatArray {
    rewind() // Rewind the buffer to read from the beginning
    val floatArray = FloatArray(this.remaining() / FLOAT_SIZE_BYTES)
    for (i in floatArray.indices) {
        floatArray[i] = this.float
    }
    return floatArray
}

fun FloatArray.toByteBuffer(): ByteBuffer {
    val byteBuffer =
        ByteBuffer.allocate(this.size * FLOAT_SIZE_BYTES) // Assuming each float is 4 bytes
    for (floatValue in this) {
        byteBuffer.putFloat(floatValue)
    }
    byteBuffer.flip() // Prepare the buffer for reading
    return byteBuffer
}


fun ByteArray.toByteBuffer(): ByteBuffer {
    return ByteBuffer.wrap(this).order(ByteOrder.nativeOrder())
}

fun ByteBuffer.toByteArray(): ByteArray {
    val byteBuffer = this.duplicate().order(ByteOrder.nativeOrder())
    val byteArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(byteArray)
    return byteArray
}

fun Bitmap.cropToBoundingBox(
    boundingBox: Rect,
    rotation: Int
): Bitmap? {
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    val rotatedImage = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    val validBoundingBox = Rect(
        boundingBox.left.coerceIn(0, rotatedImage.width),
        boundingBox.top.coerceIn(0, rotatedImage.height),
        boundingBox.right.coerceIn(0, rotatedImage.width),
        boundingBox.bottom.coerceIn(0, rotatedImage.height)
    )

    val cropWidth = (validBoundingBox.right - validBoundingBox.left).coerceAtLeast(1)
    val cropHeight = (validBoundingBox.bottom - validBoundingBox.top).coerceAtLeast(1)

    return try {
        Bitmap.createBitmap(
            rotatedImage,
            validBoundingBox.left,
            validBoundingBox.top,
            cropWidth,
            cropHeight
        )
    } catch (e: IllegalArgumentException) {
        null
    } finally {
        if (rotation != 0) {
            rotatedImage.recycle()
        }
    }
}


fun Bitmap.toGrayScale(): Bitmap {
    val mat = Mat()
    Utils.bitmapToMat(this, mat)
    this.recycle()

    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)

    val outputBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(mat, outputBitmap)

    return outputBitmap
}
