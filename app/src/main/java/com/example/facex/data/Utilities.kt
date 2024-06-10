package com.example.facex.data

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import com.google.mlkit.vision.face.Face
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toByteBuffer(): ByteBuffer {
    return ByteBuffer.wrap(this).order(ByteOrder.nativeOrder())
}

fun ByteBuffer.toByteArray(): ByteArray {
    val byteBuffer = this.duplicate().order(ByteOrder.nativeOrder())
    val byteArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(byteArray)
    return byteArray
}

fun cropToBoundingBox(image: Bitmap, boundingBox: Rect, rotation: Int = 0): Bitmap? {
    val rotatedImage = if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val newBitmap = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        image.recycle()
        newBitmap
    } else {
        image
    }

    return boundingBox.takeIf {
        it.top >= 0
                && it.bottom <= rotatedImage.width
                && it.top + it.height() <= rotatedImage.height
                && it.left >= 0
                && it.left + it.width() <= rotatedImage.width
    }?.let {
        Bitmap.createBitmap(
            rotatedImage,
            boundingBox.left,
            boundingBox.top,
            boundingBox.width(),
            boundingBox.height()
        )
    }
}
