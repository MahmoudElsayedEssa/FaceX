package com.example.facex.ui.utils

import android.content.res.Configuration
import android.graphics.Rect
import androidx.camera.core.CameraSelector
import androidx.compose.ui.geometry.Size
import kotlin.math.min


fun adjustBoundingBoxForView(
    rect: Rect,
    orientation: Int,
    imageSize: Size,
    viewSize: Size,
    cameraSelector: CameraSelector
): androidx.compose.ui.geometry.Rect {
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val isFrontMode = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

    // Adjusted width and height based on orientation
    val (adjustedWidth, adjustedHeight) = if (isLandscape) {
        Pair(imageSize.width, imageSize.height)
    } else {
        Pair(imageSize.height, imageSize.width)
    }

    // Scale factors to maintain aspect ratio
    val scale = min(viewSize.width / adjustedWidth, viewSize.height / adjustedHeight)

    // Offset for centering the bounding box in the view
    val offsetX = (viewSize.width - adjustedWidth * scale) / 2f
    val offsetY = (viewSize.height - adjustedHeight * scale) / 2f

    // Mapping the bounding box to the view
    var mappedBox = androidx.compose.ui.geometry.Rect(
        left = rect.left * scale + offsetX,
        top = rect.top * scale + offsetY,
        right = rect.right * scale + offsetX,
        bottom = rect.bottom * scale + offsetY
    )

    // Flip horizontally if front camera
    if (isFrontMode) {
        val centerX = viewSize.width / 2f
        mappedBox = mappedBox.copy(
            left = centerX + (centerX - mappedBox.right),
            right = centerX + (centerX - mappedBox.left)
        )
    }

    return mappedBox
}
