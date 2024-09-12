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
    rotation: Int = 0
): Bitmap? {
    if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotatedBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)

        val validBoundingBox = Rect(
            boundingBox.left.coerceIn(0, rotatedBitmap.width),
            boundingBox.top.coerceIn(0, rotatedBitmap.height),
            boundingBox.right.coerceIn(0, rotatedBitmap.width),
            boundingBox.bottom.coerceIn(0, rotatedBitmap.height)
        )

        val cropWidth = (validBoundingBox.right - validBoundingBox.left).coerceAtLeast(1)
        val cropHeight = (validBoundingBox.bottom - validBoundingBox.top).coerceAtLeast(1)

        return try {
            Bitmap.createBitmap(
                rotatedBitmap,
                validBoundingBox.left,
                validBoundingBox.top,
                cropWidth,
                cropHeight
            )
        } catch (e: IllegalArgumentException) {
            null
        } finally {
            rotatedBitmap.recycle()
        }
    } else {
        val validBoundingBox = Rect(
            boundingBox.left.coerceIn(0, width),
            boundingBox.top.coerceIn(0, height),
            boundingBox.right.coerceIn(0, width),
            boundingBox.bottom.coerceIn(0, height)
        )

        val cropWidth = (validBoundingBox.right - validBoundingBox.left).coerceAtLeast(1)
        val cropHeight = (validBoundingBox.bottom - validBoundingBox.top).coerceAtLeast(1)

        return try {
            Bitmap.createBitmap(
                this,
                validBoundingBox.left,
                validBoundingBox.top,
                cropWidth,
                cropHeight
            )
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}


fun Bitmap.toGrayScale(): Bitmap {
    val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(grayscaleBitmap)
    val paint = Paint()
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f) // 0 saturation for grayscale
    val filter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = filter

    canvas.drawBitmap(this, 0f, 0f, paint)

    this.recycle()
    return grayscaleBitmap
}
