package com.example.facex.ui.components

import androidx.camera.core.FocusMeteringAction
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min


@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier,
    onUpdatePreviewView: (Size,PreviewView) -> Unit,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { view ->
            view.previewStreamState.observe(lifecycleOwner) { state ->
                if (state == PreviewView.StreamState.STREAMING) {
                    val previewWidth = view.width
                    val previewHeight = view.height
                    val previewSize = Size(previewWidth.toFloat(), previewHeight.toFloat())
                    onUpdatePreviewView(previewSize,view)
                }
            }
        },
        modifier = modifier
    )
}
