package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Rect
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.facex.ui.screens.camera_face_recognition.components.DetectedFacesOverlayView

@Composable
fun CameraRecognitionScreen(
    state: RecognitionState = RecognitionState(),
    actions: RecognitionActions = RecognitionActions(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current


    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).apply {
                    actions.startCamera(this, lifecycleOwner)
                }
            }
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                DetectedFacesOverlayView(context, null)
            },
            update = { view ->
                view.setResults(state.detectedFaces)
            }
        )



//        state.detectedFaces.forEach { detectedFace ->
//            detectedFace.boundingBox?.let { DrawRectangle(rect = it) }
//        }

        // Draw recognized faces with name and confidence
//        state.recognizedFaces.forEach { recognizedFace ->
//            recognizedFace.detectedFace?.let { detectedFace ->
//                DrawRectangle(rect = detectedFace.boundingBox)
//                DrawText(
//                    text = "${recognizedFace.recognizedPerson?.person?.name}" +
//                            " - Confidence: ${recognizedFace.recognizedPerson?.confidence}",
//                    rect = detectedFace.boundingBox
//                )
//            }
//        }

//        DrawFacesBoundingBoxes(state.recognizedFaces)

    }

}


@Composable
fun DrawRectangle(rect: Rect, color: Color = Color.Red, strokeWidth: Dp = Dp(4f)) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = color,
            topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
            size = Size(rect.width().toFloat(), rect.height().toFloat()),
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}