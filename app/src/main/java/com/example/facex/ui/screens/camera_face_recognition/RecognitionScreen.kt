package com.example.facex.ui.screens.camera_face_recognition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.facex.domain.entities.CameraState
import com.example.facex.ui.screens.camera_face_recognition.components.CameraPreview
import com.example.facex.ui.screens.camera_face_recognition.components.DialogWithImage
import com.example.facex.ui.screens.camera_face_recognition.components.FacesOverlayView

@Composable
fun CameraRecognitionScreen(
    state: RecognitionState = RecognitionState(),
    actions: RecognitionActions = RecognitionActions(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var showDialog by remember { mutableStateOf(false) }
    val noFacesDetected = state.detectedFaces.isEmpty()

    val linearZoom by actions.getLinearZoom().collectAsState()
    val zoomRatio by actions.getRatioZoom().collectAsState()

    LaunchedEffect(Unit) {
        actions.startCamera(lifecycleOwner)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when (val cameraState = state.cameraState) {
            is CameraState.Active -> {
                CameraPreview(
                    preview = cameraState.preview,
                    onTapToFocus = actions.onTapToFocus,
                    onZoomRatio = { ratio ->
                        if (ratio != zoomRatio) {
                            actions.onZoomRatio(ratio)
                        }
                    },
                    currentZoomRatio = zoomRatio
                )
            }

            is CameraState.Error -> {
                Text("Error: ${cameraState.message}")
            }

            CameraState.Inactive -> {
                CircularProgressIndicator()
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                FacesOverlayView(context, null)
            },
            update = { view ->
                view.setResults(state.detectedFaces, state.recognizedFaces)
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Zoom: ${String.format("%.1f", zoomRatio)}x",
                modifier = Modifier.padding(8.dp)
            )

            Slider(
                value = linearZoom,
                onValueChange = { actions.onLinearZoom(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            if (noFacesDetected) {
                Text(
                    text = "No faces detected. Please position your face in front of the camera.",
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Button(
                onClick = {
                    showDialog = true
                },
                enabled = !noFacesDetected,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Capture Face")
            }
            Button(
                onClick = { actions.switchCamera(lifecycleOwner) },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Switch Camera")
            }

            if (showDialog) {
                DialogWithImage(
                    onDismissRequest = {
                        showDialog = false
                    },
                    onConfirmation = { name, embedding ->
                        actions.onCaptureFace(name, embedding)
                        showDialog = false
                    },
                    detectedFace = state.detectedFaces.firstOrNull()
                )
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // actions.onStopRecognition()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // Cleanup when the effect is disposed
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
