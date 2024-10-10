package com.example.facex.ui.screens.camera_face_recognition

import ImageAnalysisPreview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        ImageAnalysisPreview(state = state, actions = actions, onFaceTapped = { df ->
            tappedFace = df
            showDialog = true
        })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (showDialog) {
                tappedFace?.let { tf ->
                    DialogWithImage(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = { name ->
                            actions.onCaptureFace(
                                name,
                                tf.imageByteBuffer,
                                tf.boundingBox.width(),
                                tf.boundingBox.height(),
                            )
                            showDialog = false
                        },
                        trackedFace = tf
                    )
                }
            }
        }
    }
}
