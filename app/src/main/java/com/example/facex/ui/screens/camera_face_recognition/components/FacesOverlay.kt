package com.example.facex.ui.screens.camera_face_recognition.components

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.example.facex.ui.FaceState
import com.example.facex.ui.TrackedFace
import com.example.facex.ui.utils.adjustBoundingBoxForView


@Composable
fun FacesOverlay(
    trackedFaces: List<TrackedFace>,
    cameraSelector: CameraSelector,
    cameraPreviewSize: android.util.Size,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    // Use remember to keep track of face recognition states across recompositions
    val faceStates = remember { mutableStateMapOf<Int, FaceState>() }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            val imageSize =
                Size(cameraPreviewSize.width.toFloat(), cameraPreviewSize.height.toFloat())
            val viewSize = Size(size.width, size.height)

            trackedFaces.forEach { face ->
                val adjustedBox = adjustBoundingBoxForView(
                    face.boundingBox,
                    orientation,
                    imageSize,
                    viewSize,
                    cameraSelector = cameraSelector
                )

                val faceState = faceStates.getOrPut(face.id) { FaceState() }
                faceState.update(face.isRecognized, face.displayName, face.confidence)

                drawTrackedFace(faceState, adjustedBox, context)
            }

            // Clean up old face states
            faceStates.keys.retainAll { id -> trackedFaces.any { it.id == id } }
        }
    }
}


private fun DrawScope.drawTrackedFace(faceState: FaceState, adjustedBox: Rect, context: Context) {
    var color = if (faceState.isRecognized) Color.Green else Color(
        ContextCompat.getColor(
            context,
            R.color.purple_200
        )
    )

    // Draw bounding box
    drawRoundRect(
        color = color,
        topLeft = Offset(adjustedBox.left, adjustedBox.top),
        size = Size(adjustedBox.width, adjustedBox.height),
        style = Stroke(width = 8f),
        cornerRadius = CornerRadius(32f, 32f)
    )

    // Draw name if recognized
    if (faceState.isRecognized) {
        val name = "${faceState.displayName} (${faceState.confidence.format(2)})"
        val textSize = 50f  // Increase this value to make the text bigger
        val paint = android.graphics.Paint().apply {
            color = Color.White  // Use Android's Color class for text color
            this.textSize = textSize
            isAntiAlias = true  // Smooth edges for better readability
            strokeWidth = 4f  // Optionally add a stroke width to make the text stand out more
            style = android.graphics.Paint.Style.FILL  // Define the style (e.g., FILL, STROKE)
            typeface =
                android.graphics.Typeface.DEFAULT_BOLD  // Set the typeface to bold if desired
            textAlign = android.graphics.Paint.Align.LEFT
        }

        // Measure the width of the text
        val textWidth = paint.measureText(name)
        val textHeight = paint.fontMetrics.bottom - paint.fontMetrics.top

        // Draw name background
        drawRoundRect(
            color = Color.Transparent,
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
            adjustedBox.left + BOUNDING_RECT_TEXT_PADDING / 2,  // Center text horizontally within the padding
            adjustedBox.top + textHeight,  // Draw the text inside the background
            paint
        )
    }
}

// Keep the adjustBoundingBoxForView function as is

private const val BOUNDING_RECT_TEXT_PADDING = 8f
private const val IMAGE_WIDTH = 640f
private const val IMAGE_HEIGHT = 480f

fun Double.format(digits: Int) = "%.${digits}f".format(this)