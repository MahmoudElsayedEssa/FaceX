package com.example.facex.ui.components


import android.util.Size
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier,
    onPreviewSizeChanged: (Size) -> Unit // Callback to notify size change
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val zoomState = controller.zoomState.observeAsState()
    var zoomRatio by remember { mutableFloatStateOf(zoomState.value?.zoomRatio ?: 1f) }
    zoomRatio = zoomState.value?.zoomRatio ?: 1f
    val maxZoomRatio = zoomState.value?.maxZoomRatio ?: 10f

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        this.controller = controller
                        controller.bindToLifecycle(lifecycleOwner)

                        // Set a surfaceProvider to get the preview size
                        Preview.SurfaceProvider { request ->
                            val previewSize = request.resolution
                            onPreviewSizeChanged(previewSize)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val meteringPointFactory = (this as PreviewView).meteringPointFactory
                            val point = meteringPointFactory.createPoint(offset.x, offset.y)
                            val action = FocusMeteringAction
                                .Builder(point)
                                .build()
                            controller.cameraControl?.startFocusAndMetering(action)
                        }
                    }
            )
        }
        Slider(
            value = zoomRatio,
            onValueChange = {
                zoomRatio = it
                controller.setZoomRatio(it)
            },
            valueRange = 1f..maxZoomRatio,
            modifier = Modifier.padding(16.dp)
        )
    }
}
