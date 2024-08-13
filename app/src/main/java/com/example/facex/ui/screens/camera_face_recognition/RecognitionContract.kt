package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.RecognizedPerson

data class RecognitionState(
    val recognizedFaces: List<RecognizedPerson> = emptyList(),
    val detectedFaces: List<DetectedFace> = emptyList(),
)


data class RecognitionActions(
    val navigateToUploadScreen: () -> Unit = {},
    val onCaptureFace: (name: String, embedding: Embedding) -> Unit = { _, _ -> },
    val onStopRecognition: () -> Unit = {},
    val onAnalysis: (Bitmap, Int) -> Unit = { _, _ -> }
)