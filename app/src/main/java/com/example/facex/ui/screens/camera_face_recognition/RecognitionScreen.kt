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
import com.example.facex.ui.TrackedFace
import com.example.facex.ui.screens.camera_face_recognition.components.DialogWithImage

@Composable
fun CameraRecognitionScreen(
    state: RecognitionState = RecognitionState(),
    actions: RecognitionActions = RecognitionActions(),
) {
    var showDialog by remember { mutableStateOf(false) }
    var tappedFace by remember { mutableStateOf<TrackedFace?>(null) }

    Box {
        ImageAnalysisPreview(
            state = state,
            actions = actions,
            onFaceTapped = { face ->
                Log.d("NONI", "tapppping: ")
                tappedFace = face
                showDialog = true
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (showDialog) {
                tappedFace?.let { detectedFace ->
                    DialogWithImage(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = { name ->
                            actions.onCaptureFace(name, detectedFace.bitmap)
                            showDialog = false
                        },
                        detectedFaceBitmap = detectedFace.bitmap
                    )
                }
            }
        }
    }
}
