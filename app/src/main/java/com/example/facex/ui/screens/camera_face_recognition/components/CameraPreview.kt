package com.example.facex.ui.screens.camera_face_recognition.components


import android.view.ViewGroup
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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


@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
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