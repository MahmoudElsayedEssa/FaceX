package com.example.facex.ui.screens.camera_face_recognition.components

import android.content.res.Configuration
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.facex.R
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun FacesOverlay(
    detectedFaces: List<DetectedFace?>,
    recognizedPersons: List<RecognizedPerson>,
    cameraSelector: CameraSelector,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val imageSize = Size(IMAGE_WIDTH, IMAGE_HEIGHT)
            val viewSize = Size(size.width, size.height)

            detectedFaces.forEach { face ->
                val adjustedBox =
                    face?.boundingBox?.let {
                        adjustBoundingBoxForView(
                            it,
                            orientation,
                            imageSize,
                            viewSize,
                            cameraSelector = cameraSelector
                        )
                    }
                if (adjustedBox != null) {
                    drawRoundRect(
                        color = Color(ContextCompat.getColor(context, R.color.purple_200)),
                        topLeft = Offset(adjustedBox.left, adjustedBox.top),
                        size = Size(adjustedBox.width, adjustedBox.height),
                        style = Stroke(width = 8f),
                        cornerRadius = CornerRadius(32f, 32f)
                    )
                }
            }

            recognizedPersons.forEach { person ->
                val adjustedBox = adjustBoundingBoxForView(
                    person.detectedFace.boundingBox,
                    orientation,
                    imageSize,
                    viewSize,
                    cameraSelector = cameraSelector
                )
                drawRecognizedPerson(person, adjustedBox)
            }
        }
    }
}

private fun DrawScope.drawRecognizedPerson(person: RecognizedPerson, adjustedBox: Rect) {
    // Draw bounding box
//    drawRect(
//        color = Color.Green,
//        topLeft = Offset(adjustedBox.left, adjustedBox.top),
//        size = Size(adjustedBox.width, adjustedBox.height),
//        style = Stroke(width = 8f)
//    )

    drawRoundRect(
        color = Color.Green,
        topLeft = Offset(adjustedBox.left, adjustedBox.top),
        size = Size(adjustedBox.width, adjustedBox.height),
        style = Stroke(width = 8f),
        cornerRadius = CornerRadius(32f, 32f)
    )

    // Draw name background
    val name = person.person.name
    var textSize = 50f
    val textWidth = name.length * textSize // Approximate width
    val textHeight = textSize
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(adjustedBox.left, adjustedBox.top),
        size = Size(
            textWidth + BOUNDING_RECT_TEXT_PADDING,
            textHeight + BOUNDING_RECT_TEXT_PADDING
        ),
        cornerRadius = CornerRadius(32f, 32f)
    )

    // Draw name
    drawContext.canvas.nativeCanvas.drawText(
        name,
        adjustedBox.left,
        adjustedBox.top + textHeight,
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 50f
        }
    )
}

private fun adjustBoundingBoxForView(
    rect: android.graphics.Rect,
    orientation: Int,
    imageSize: Size,
    viewSize: Size,
    cameraSelector: CameraSelector,
): Rect {
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val isFrontMode = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

    val (adjustedWidth, adjustedHeight) = if (isLandscape) {
        Pair(imageSize.width, imageSize.height)
    } else {
        Pair(imageSize.height, imageSize.width)
    }

    val scaleX = viewSize.width / adjustedWidth
    val scaleY = viewSize.height / adjustedHeight

    val scale = max(scaleX, scaleY)

    val offsetX = (viewSize.width - ceil(adjustedWidth * scale)) / 2f
    val offsetY = (viewSize.height - ceil(adjustedHeight * scale)) / 2f

    var mappedBox = Rect(
        left = (rect.left * scale) + offsetX,
        top = (rect.top * scale) + offsetY,
        right = (rect.right * scale) + offsetX,
        bottom = (rect.bottom * scale) + offsetY
    )

    if (isFrontMode) {
        val centerX = viewSize.width / 2f
        mappedBox = mappedBox.copy(
            left = centerX + (centerX - mappedBox.left),
            right = centerX - (mappedBox.right - centerX)
        )
    }

    return mappedBox
}

private const val BOUNDING_RECT_TEXT_PADDING = 8f
private const val IMAGE_WIDTH = 640f
private const val IMAGE_HEIGHT = 480f