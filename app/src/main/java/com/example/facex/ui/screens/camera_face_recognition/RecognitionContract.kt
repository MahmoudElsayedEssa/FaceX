package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Rect
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson

data class RecognitionState(
    val recognizedFaces: List<RecognizedFace> = emptyList(),
    val detectedFaces: List<DetectedFace> = emptyList(),
)


data class RecognizedFace(
    val detectedFace: DetectedFace? = null,
    val recognizedPerson: RecognizedPerson? = null
)




data class RecognitionActions(
    val startCamera: (previewView: PreviewView, lifecycleOwner: LifecycleOwner) -> Unit = { _, _ -> },
    val navigateToUploadScreen: () -> Unit = {},
)

