package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import com.example.facex.domain.entities.PerformanceMetrics
import com.example.facex.ui.FrameData
import com.example.facex.ui.TrackedFace

@Stable
data class RecognitionState(
    val trackedFaces: Map<Int, TrackedFace> = emptyMap(),
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics()
)


data class RecognitionActions(
    val navigateToUploadScreen: () -> Unit = {},
    val onCaptureFace: (name: String, faceBitmap: Bitmap) -> Unit = { _, _ -> },
    val onStopRecognition: () -> Unit = {},
    val onAnalysis: (FrameData) -> Unit = { }
)


