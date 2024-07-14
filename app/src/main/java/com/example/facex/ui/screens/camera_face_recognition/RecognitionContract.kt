package com.example.facex.ui.screens.camera_face_recognition

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import java.nio.ByteBuffer

data class RecognitionState(
    val recognizedFaces: List<RecognizedPerson> = emptyList(),
    val detectedFaces: List<DetectedFace> = emptyList(),
)


data class RecognitionActions(
    val onStartCamera: (previewView: PreviewView, lifecycleOwner: LifecycleOwner) -> Unit = { _, _ -> },
    val onCaptureFace: (name: String, embedding: ByteBuffer) -> Unit = { _, _ -> },
    val navigateToUploadScreen: () -> Unit = {},
    val onStopRecognition: () -> Unit = {},
)