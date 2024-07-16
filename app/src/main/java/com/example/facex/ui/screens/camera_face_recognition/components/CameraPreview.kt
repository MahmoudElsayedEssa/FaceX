package com.example.facex.ui.screens.camera_face_recognition.components


import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun CameraPreview(
    preview: Preview,
    onTapToFocus: (Float, Float) -> Unit,
    onZoomRatio: (Float) -> Unit,
    currentZoomRatio: Float
) {
    var lastZoomRatio by remember { mutableFloatStateOf(currentZoomRatio) }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }.also { previewView ->
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onTapToFocus(offset.x, offset.y)
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newZoomRatio = lastZoomRatio * zoom

                    if (newZoomRatio != lastZoomRatio && newZoomRatio > 1f) {
                        lastZoomRatio = newZoomRatio
                        onZoomRatio(newZoomRatio)
                    }
                }
            }
    )
}
