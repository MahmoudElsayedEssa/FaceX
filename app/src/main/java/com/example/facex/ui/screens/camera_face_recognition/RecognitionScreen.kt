package com.example.facex.ui.screens.camera_face_recognition

import ImageAnalysisPreview
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.facex.ui.screens.camera_face_recognition.components.DialogWithImage
import com.example.facex.ui.screens.camera_face_recognition.components.FacesImageAnalyzer

@Composable
fun CameraRecognitionScreen(
    state: RecognitionState = RecognitionState(),
    actions: RecognitionActions = RecognitionActions(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val applicationContext = LocalContext.current.applicationContext
    var showDialog by remember { mutableStateOf(false) }
    val noFacesDetected = state.detectedFaces.isEmpty()

    var cameraSelector by remember { mutableStateOf(DEFAULT_FRONT_CAMERA) }
    val analyzer = remember {
        FacesImageAnalyzer(applicationContext, actions.onAnalysis)
    }

    val controller = remember {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(applicationContext),
                analyzer
            )

            this.cameraSelector = cameraSelector
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageAnalysisPreview(
            state = state,
            actions = actions
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
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_PAUSE) {
//                // actions.onStopRecognition()
//            }
//        }
//
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        // Cleanup when the effect is disposed
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
}
