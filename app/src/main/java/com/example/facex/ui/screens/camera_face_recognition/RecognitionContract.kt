package com.example.facex.ui.screens.camera_face_recognition

import androidx.compose.runtime.Stable
import com.example.facex.domain.entities.PerformanceMetrics
import com.example.facex.ui.FrameData
import com.example.facex.ui.TrackedFace
import java.nio.ByteBuffer

@Stable
data class RecognitionState(
    val trackedFaces:  List<TrackedFace> = emptyList(),
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics()
)


data class RecognitionActions(
    val navigateToUploadScreen: () -> Unit = {},
    val onCaptureFace: (name: String, faceByteBuffer: ByteBuffer, width: Int, height: Int) -> Unit = { _, _, _, _ -> },
    val onStopRecognition: () -> Unit = {},
    val onAnalysis: (FrameData) -> Unit = { }
)


