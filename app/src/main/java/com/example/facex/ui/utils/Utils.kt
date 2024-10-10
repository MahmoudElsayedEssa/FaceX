package com.example.facex.ui.utils

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.ui.geometry.Size
import com.example.facex.domain.entities.Person
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import androidx.compose.ui.geometry.Rect as ComposeRect

fun adjustBoundingBoxForView(
    rect: Rect,
    imageSize: Size,
    viewSize: Size,
    isLandscape: Boolean,
    isFrontMode: Boolean
): androidx.compose.ui.geometry.Rect {

    // Adjusted width and height based on orientation
    val (adjustedWidth, adjustedHeight) = if (isLandscape) {
        Pair(imageSize.width, imageSize.height)
    } else {
        Pair(imageSize.height, imageSize.width)
    }
    Log.d("MAMOOO", "adjustBoundingBoxForView: imageSize:$imageSize ,viewSize ")
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


fun adjustBoundingBoxForView2(
    rect: Rect,
    analysisSize: Size,
    viewSize: Size,
    cameraSelector: CameraSelector,
    orientation: Int
): ComposeRect {
    val isFrontMode = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    // Calculate scale factors
    val scaleX = viewSize.width / analysisSize.width
    val scaleY = viewSize.height / analysisSize.height
    val scale = minOf(scaleX, scaleY)

    // Calculate offsets to center the image
    val offsetX = (viewSize.width - analysisSize.width * scale) / 2f
    val offsetY = (viewSize.height - analysisSize.height * scale) / 2f

    // Scale and translate the rectangle
    val scaledRect = RectF(
        rect.left * scale + offsetX,
        rect.top * scale + offsetY,
        rect.right * scale + offsetX,
        rect.bottom * scale + offsetY
    )

    // Handle front camera mirroring
    val mirroredRect = if (isFrontMode) {
        RectF(
            viewSize.width - scaledRect.right,
            scaledRect.top,
            viewSize.width - scaledRect.left,
            scaledRect.bottom
        )
    } else scaledRect

    // Handle orientation
    val rotatedRect = if (isLandscape) {
        RectF(
            mirroredRect.top,
            viewSize.height - mirroredRect.right,
            mirroredRect.bottom,
            viewSize.height - mirroredRect.left
        )
    } else mirroredRect

    Log.d("NOOON", "adjustBoundingBoxForView2: Final rect: $rotatedRect")

    return ComposeRect(
        left = rotatedRect.left,
        top = rotatedRect.top,
        right = rotatedRect.right,
        bottom = rotatedRect.bottom
    )
}

data class AdjustedBoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val centerX: Float,
    val centerY: Float
)

fun adjustBoundingBox(
    boundingBox: Rect,
    analysisSize: Size,
    canvasSize: Size,
    isFrontCamera: Boolean
): AdjustedBoundingBox {
    val scaleX = canvasSize.width / analysisSize.width
    val scaleY = canvasSize.height / analysisSize.height
    val scaleFactor = min(scaleX, scaleY)

    val scaledWidth = analysisSize.width * scaleFactor
    val scaledHeight = analysisSize.height * scaleFactor
    val dx = (canvasSize.width - scaledWidth) / 2
    val dy = (canvasSize.height - scaledHeight) / 2

    var scaledLeft = boundingBox.left * scaleFactor + dx
    var scaledTop = boundingBox.top * scaleFactor + dy
    var scaledRight = boundingBox.right * scaleFactor + dx
    var scaledBottom = boundingBox.bottom * scaleFactor + dy

    if (isFrontCamera) {
        val mirroredLeft = canvasSize.width - scaledRight
        val mirroredRight = canvasSize.width - scaledLeft
        scaledLeft = mirroredLeft
        scaledRight = mirroredRight
    }

    val minSize = 100f
    val horizontalExpansionFactor = 2.7f
    val verticalExpansionFactor = 2.7f
    val width = max(scaledRight - scaledLeft, minSize)
    val height = max(scaledBottom - scaledTop, minSize)
    val centerX = (scaledLeft + scaledRight) / 2
    val centerY = (scaledTop + scaledBottom) / 2

    scaledLeft = centerX - (width * horizontalExpansionFactor) / 2
    scaledRight = centerX + (width * horizontalExpansionFactor) / 2
    scaledTop = centerY - (height * verticalExpansionFactor) / 2
    scaledBottom = centerY + (height * verticalExpansionFactor) / 2

    val shiftY = height * (0.4f - (height / canvasSize.height) * 0.2f)
    scaledTop -= shiftY
    scaledBottom -= shiftY

    scaledLeft = max(0f, scaledLeft)
    scaledTop = max(0f, scaledTop).toFloat()
    scaledRight = min(canvasSize.width, scaledRight)
    scaledBottom = min(canvasSize.height, scaledBottom)

    return AdjustedBoundingBox(scaledLeft, scaledTop, scaledRight, scaledBottom, centerX, centerY)
}


fun adjustBoundingBoxForView3(
    rect: Rect,
    imageSize: Size,
    viewSize: Size,
    cameraSelector: CameraSelector
): androidx.compose.ui.geometry.Rect {
    val isFrontMode = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

    // Calculate scale factors
    val scaleX = viewSize.width / imageSize.width.toFloat() / 2f
    val scaleY = viewSize.height / imageSize.height.toFloat() / 2f

    // Map the coordinates directly
    var left = rect.left * scaleX
    var top = rect.top * scaleY
    var right = rect.right * scaleX
    var bottom = rect.bottom * scaleY

    // Flip horizontally if front camera
    if (isFrontMode) {
        val tmp = left
        left = viewSize.width - right
        right = viewSize.width - tmp
    }

    // Create the final mapped box
    val mappedBox = androidx.compose.ui.geometry.Rect(
        left = left,
        top = top,
        right = right,
        bottom = bottom
    )

    // Log input and output for debugging
    Log.d(
        "BoundingBox",
        "Input: rect=$rect, imageSize=$imageSize, viewSize=$viewSize, isFrontMode=$isFrontMode"
    )
    Log.d("BoundingBox", "Calculated: scaleX=$scaleX, scaleY=$scaleY")
    Log.d("BoundingBox", "Output: mappedBox=$mappedBox")

    return mappedBox
}


fun Bitmap.scale(scaleFactor: Float): Bitmap {
    val width = (this.width * scaleFactor).toInt()
    val height = (this.height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(this, width, height, true)
}

fun Rect.scale(scaleFactor: Float): Rect {
    return Rect(
        (left * scaleFactor).toInt(),
        (top * scaleFactor).toInt(),
        (right * scaleFactor).toInt(),
        (bottom * scaleFactor).toInt()
    )
}


fun Long.formatTime(): String {
    val milliseconds = this / 1_000_000
    val seconds = milliseconds / 1_000
    val minutes = seconds / 60

    return when {
        minutes > 0 -> String.format(Locale.US, "%d min %d sec", minutes, seconds % 60)
        seconds > 0 -> String.format(Locale.US, "%d sec %d ms", seconds, milliseconds % 1_000)
        milliseconds > 0 -> String.format(
            Locale.US,
            "%d ms %d µs",
            milliseconds,
            (this % 1_000_000) / 1_000
        )

        else -> String.format(Locale.US, "%d ns", this)
    }
}


fun Duration.formatDuration(): String {
    return when {
        this < 1.microseconds -> "${inWholeMicroseconds}ns"
        this < 1.milliseconds -> "${inWholeMicroseconds}µs"
        this < 1.seconds -> "${inWholeMilliseconds}ms"
        else -> this.toString()
    }
}
