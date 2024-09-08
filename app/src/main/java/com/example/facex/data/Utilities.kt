package com.example.facex.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect

fun Bitmap.cropToBoundingBox(
    boundingBox: Rect,
    rotation: Int = 90
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
    // Create a mutable Bitmap to draw on
    val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Create a Canvas to draw onto the new Bitmap
    val canvas = Canvas(grayscaleBitmap)

    // Create a Paint object with a ColorMatrix to transform the image to grayscale
    val paint = Paint()
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f) // 0 saturation for grayscale
    val filter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = filter

    // Draw the original Bitmap onto the new one with the grayscale filter
    canvas.drawBitmap(this, 0f, 0f, paint)

    // Recycle the original bitmap to free up memory
    this.recycle()

    return grayscaleBitmap
}
