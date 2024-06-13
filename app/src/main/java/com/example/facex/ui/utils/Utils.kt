package com.example.facex.ui.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.RectF
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.ui.geometry.Size
import kotlin.math.ceil

fun adjustBoundingBoxForView(
    rect: Rect,
    orientation: Int,
    imageSize: Size,
    viewSize: Size,
    cameraSelector: Int = CameraSelector.LENS_FACING_BACK,
): RectF {


    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val isFrontMode = cameraSelector == CameraSelector.LENS_FACING_FRONT

    val (adjustedWidth, adjustedHeight) = if (isLandscape) {
        Pair(imageSize.width, imageSize.height)
    } else {
        Pair(imageSize.height, imageSize.width)
    }

    val scaleX = viewSize.width / adjustedWidth
    val scaleY = viewSize.height / adjustedHeight


    val scale = maxOf(scaleX, scaleY)

    val offsetX = (viewSize.width - ceil(adjustedWidth * scale)) / 2.0f
    val offsetY = (viewSize.height - ceil(adjustedHeight * scale)) / 2.0f



    val mappedBox = RectF(
        (rect.right.toFloat() * scale) + offsetX,
        (rect.top.toFloat() * scale) + offsetY,
        (rect.left.toFloat() * scale) + offsetX,
        (rect.bottom.toFloat() * scale) + offsetY
    )

    if (isFrontMode) {
        val centerX = viewSize.width / 2f
        mappedBox.apply {
            left = centerX + (centerX - left)
            right = centerX - (right - centerX)
        }
    }

    return mappedBox
}


fun getRotationDegreesFromImageUri(context: Context, imageUri: Uri): Int {
    val inputStream = context.contentResolver.openInputStream(imageUri)
    val exifInterface = ExifInterface(inputStream!!)
    inputStream.close()

    return when (exifInterface.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}


fun adjustBoundingBoxForView(
    rect: Rect,
    imageSize: Size,
    viewSize: Size,
): RectF {
    val imageAspectRatio = imageSize.width / imageSize.height
    val viewAspectRatio = viewSize.width / viewSize.height

    val scale: Float
    val offsetX: Float
    val offsetY: Float

    if (imageAspectRatio > viewAspectRatio) {
        scale = viewSize.width / imageSize.width
        offsetX = 0f
        offsetY = (viewSize.height - imageSize.height * scale) / 2.0f
    } else {
        scale = viewSize.height / imageSize.height
        offsetX = (viewSize.width - imageSize.width * scale) / 2.0f
        offsetY = 0f
    }

    return RectF(
        (rect.left * scale) + offsetX,
        (rect.top * scale) + offsetY,
        (rect.right * scale) + offsetX,
        (rect.bottom * scale) + offsetY
    )
}
