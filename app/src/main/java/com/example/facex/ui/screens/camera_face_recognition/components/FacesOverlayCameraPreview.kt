package com.example.facex.ui.screens.camera_face_recognition.components

import FacesOverlay
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.facex.ui.screens.camera_face_recognition.UIFace

@Composable
fun FacesOverlayCameraPreview(
    modifier: Modifier = Modifier,
    controller: LifecycleCameraController,
    faces: List<UIFace>,
    isFlippedHorizontally: Boolean = false,
    onFaceTap: (UIFace) -> Unit,
    isLandscape: Boolean
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var previewWidth by remember { mutableIntStateOf(0) }
    var previewHeight by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier,
    ) {
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
                        previewWidth = view.width
                        previewHeight = view.height
                    }
                }

            },
            modifier = modifier,
        )

        if (previewHeight != 0 && previewWidth != 0) {
            FacesOverlay(
                faces = faces,
//                analyzerWidth = 480,
//                analyzerHeight = 640,
                previewHeight = previewHeight,
                previewWidth = previewWidth,
                isFlippedHorizontally = isFlippedHorizontally,
                isLandscape = isLandscape,
                onFaceTap = onFaceTap,
                modifier = modifier
            )
        }
    }
}
