package com.example.facex.ui.screens.camera_face_recognition

import ImageAnalysisPreview
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.example.facex.ui.screens.camera_face_recognition.components.DialogWithImage

@Composable
fun CameraRecognitionScreen(
    state: RecognitionState = RecognitionState(),
    actions: RecognitionActions = RecognitionActions(),
) {
    var showDialog by remember { mutableStateOf(false) }
    val noFacesDetected = state.trackedFaces.values.isEmpty()

    Box {
        ImageAnalysisPreview(
            state = state,
            actions = actions
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                     Log.d("NONI", "CameraRecognitionScreen: ")
                 state.trackedFaces.values.firstOrNull()?.let { detectedFace ->
                     Log.d("NONI", "CameraRecognitionScreen:detectedFace$detectedFace ")
                     DialogWithImage(
                         onDismissRequest = {
                             showDialog = false
                         },
                         onConfirmation = { name ->
                             actions.onCaptureFace(name,detectedFace.bitmap)
                             showDialog = false
                         },
                         detectedFaceBitmap = detectedFace.bitmap
                     )

                 }

            }
        }
    }
}
