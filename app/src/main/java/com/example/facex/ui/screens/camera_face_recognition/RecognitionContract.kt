package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.ui.FrameData
import com.example.facex.ui.TrackedFace

data class RecognitionState(
    val recognizedFaces: List<RecognizedPerson> = emptyList(),
    val detectedFaces: List<DetectedFace?> = emptyList(),
    val trackedFaces: Map<Int, TrackedFace> = emptyMap()
    ,
)


data class RecognitionActions(
    val navigateToUploadScreen: () -> Unit = {},
    val onCaptureFace: (name: String,faceBitmap: Bitmap) -> Unit = {_,_->},
    val onStopRecognition: () -> Unit = {},
    val onAnalysis: (FrameData) -> Unit = { }
)