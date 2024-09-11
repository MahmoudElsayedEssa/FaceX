package com.example.facex.ui.screens.camera_face_recognition.components

import android.content.res.Configuration
import android.graphics.Paint
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ZoomState
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.example.facex.ui.TrackedFace
import com.example.facex.ui.utils.adjustBoundingBoxForView
import java.util.concurrent.TimeUnit


@Composable
fun FacesOverlay(
    trackedFaces: List<TrackedFace>,
    cameraSelector: CameraSelector,
    analysisSize: Size,
    previewSize: Size,
    onFaceTapped: (TrackedFace) -> Unit,
    modifier: Modifier = Modifier,
) {
    val orientation = LocalConfiguration.current.orientation
    val isFrontCamera = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    Canvas(
        modifier = modifier
            .interceptTapsOnFaces(
                trackedFaces = trackedFaces,
                analysisSize = analysisSize,
                previewSize = previewSize,
                isLandscape = isLandscape,
                isFrontCamera = isFrontCamera,
                onFaceTapped = onFaceTapped
            )

    ) {
        trackedFaces.forEach { face ->
            val adjustedBox = adjustBoundingBoxForView(
                face.boundingBox,
                analysisSize,
                previewSize,
                isLandscape = isLandscape,
                isFrontMode = isFrontCamera
            )
            drawAngularCorners(adjustedBox, face.isRecognized)
            drawFaceLabel(adjustedBox, face.displayName)
        }
    }
}

// Custom modifier to intercept taps only on face areas
fun Modifier.interceptTapsOnFaces(
    trackedFaces: List<TrackedFace>,
    analysisSize: Size,
    previewSize: Size,
    isLandscape: Boolean,
    isFrontCamera: Boolean,
    onFaceTapped: (TrackedFace) -> Unit
) = pointerInput(trackedFaces) {
    awaitEachGesture {
        val down = awaitFirstDown()
        val tap = waitForUpOrCancellation()
        if (tap != null) {
            trackedFaces.forEach { face ->
                val adjustedBox = adjustBoundingBoxForView(
                    face.boundingBox,
                    analysisSize,
                    previewSize,
                    isLandscape = isLandscape,
                    isFrontMode = isFrontCamera
                )
                if (adjustedBox.contains(tap.position) && !face.isRecognized) {
                    onFaceTapped(face)
                    // Consume the event to prevent the camera preview from receiving it
                    return@awaitEachGesture
                }
            }
        }
    }
    // Return false if no face is tapped so the event can be passed to the camera preview
    false
}

fun Modifier.combinedPointerInput(
    trackedFaces: List<TrackedFace>,
    analysisSize: Size,
    previewSize: Size,
    isLandscape: Boolean,
    isFrontCamera: Boolean,
    onFaceTapped: (TrackedFace) -> Unit,
    zoom: LiveData<ZoomState>,
    onZoomChange: (Float) -> Unit,
    previewView: PreviewView,
    onFocusTap: (FocusMeteringAction) -> Unit,
) = this
    .pointerInput(Unit) {
        detectTransformGestures { _, _, gestureZoom, _ ->
            val zoomRatio = zoom.value?.zoomRatio
            var zoomChange = gestureZoom - 1f // Convert zoom factor to a delta
            val newZoom = (zoomRatio?.plus(zoomChange))?.coerceIn(1f, 5f) // Assuming max zoom is 5x
            if (newZoom != null) {
                onZoomChange(newZoom)
            }
        }
    }
    .pointerInput(trackedFaces) {
        awaitEachGesture {
            val down = awaitFirstDown()
            val tap = waitForUpOrCancellation()

            if (tap != null) {
                var faceTapped = false

                trackedFaces.forEach { face ->
                    val adjustedBox = adjustBoundingBoxForView(
                        face.boundingBox,
                        analysisSize,
                        previewSize,
                        isLandscape = isLandscape,
                        isFrontMode = isFrontCamera
                    )
                    if (adjustedBox.contains(tap.position) && !face.isRecognized) {
                        onFaceTapped(face)
                        // Consume the event to prevent the camera preview from receiving it
                        return@awaitEachGesture
                    }
                }
                if (!faceTapped) {
                    // Handle zoom and focus in CameraPreview

                    val meteringPointFactory = previewView.meteringPointFactory
                    val focusPoint =
                        meteringPointFactory.createPoint(tap.position.x, tap.position.y)
                    val action =
                        FocusMeteringAction
                            .Builder(focusPoint, FocusMeteringAction.FLAG_AF)
                            .setAutoCancelDuration(5, TimeUnit.SECONDS)
                            .build()
                    onFocusTap(action)
                }
            }
        }
    }


private fun DrawScope.drawFaceBoundingBox(box: Rect) {
    drawRoundRect(
        color = Color.Green,
        topLeft = Offset(box.left, box.top),
        size = Size(box.width, box.height),
        style = Stroke(width = 1.dp.toPx()),
        cornerRadius = CornerRadius(32f, 32f)
    )
}

private fun DrawScope.drawFaceLabel(box: Rect, label: String) {
    drawContext.canvas.nativeCanvas.apply {
        drawText(
            label,
            box.left,
            box.top - 10,
            Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 30f
                isAntiAlias = true
            }
        )
    }
}

private fun DrawScope.drawAngularCorners(box: Rect, isRecognized: Boolean) {
    val cornerSize = 25.dp.toPx()
    val cornerColor = if (isRecognized) Color.Green else Color.Red
    val strokeWidth = 2.dp.toPx()

    // Draw hollow corners
    drawLine(
        color = cornerColor,
        start = Offset(box.left, box.top),
        end = Offset(box.left + cornerSize, box.top),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = cornerColor,
        start = Offset(box.left, box.top),
        end = Offset(box.left, box.top + cornerSize),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = cornerColor,
        start = Offset(box.right, box.top),
        end = Offset(box.right - cornerSize, box.top),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = cornerColor,
        start = Offset(box.right, box.top),
        end = Offset(box.right, box.top + cornerSize),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = cornerColor,
        start = Offset(box.left, box.bottom),
        end = Offset(box.left + cornerSize, box.bottom),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = cornerColor,
        start = Offset(box.left, box.bottom),
        end = Offset(box.left, box.bottom - cornerSize),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = cornerColor,
        start = Offset(box.right, box.bottom),
        end = Offset(box.right - cornerSize, box.bottom),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = cornerColor,
        start = Offset(box.right, box.bottom),
        end = Offset(box.right, box.bottom - cornerSize),
        strokeWidth = strokeWidth
    )
}

private enum class CornerDirection {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}
