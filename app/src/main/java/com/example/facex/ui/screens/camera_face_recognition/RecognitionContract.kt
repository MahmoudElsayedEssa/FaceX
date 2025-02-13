package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Stable
import com.example.facex.domain.entities.FaceRecognitionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.entities.RecognitionStatus
import com.example.facex.domain.entities.Rectangle
import com.example.facex.domain.performancetracking.MetricData
import com.example.facex.ui.helpers.FacesImageAnalyzer
import com.example.facex.ui.utils.calculateFPS
import kotlinx.coroutines.CoroutineScope
import java.nio.ByteBuffer

data class RecognitionState(
    val faces: List<UIFace> = emptyList(),
    val framesPerSecond: Int = 0,
    val activeDialog: ActiveDialog = ActiveDialog.NONE,
    val selectedFace: UIFace? = null,
    val capturedFaceData: CapturedFaceData? = null,
)



data class CapturedFaceData(
    val image: Bitmap? = null,
    val frameData: Frame? = null
)

@Stable
data class UIFace(
    val id: Int,
    val boundingBox: Rect,
    val recognitionState: UIRecognitionStatus,
)

data class FrameData(
    val id: Int,
    var data: ByteBuffer,
    val rotationDegrees: Int,
    val width: Int,
    val height: Int,
)


enum class ActiveDialog { NONE, REGISTRATION, FACE_DETAILS, PERFORMANCE, MODEL_CONTROL }



data class RecognitionActions(
    val onRegisterFace: (Frame, String) -> Unit,
    val onTapFace: (UIFace) -> Unit = { _ -> },
    val onDismissDialog: () -> Unit = {},
    val onShowPerformanceBottomSheet: () -> Unit = {},
    val onShowModelControlBottomSheet: () -> Unit = {},
    val onStopRecognition: () -> Unit = {},
    val onShowFaceDetailsDialog: () -> Unit = {},
    val onShowRegistrationDialog: (UIFace, Frame,Boolean) -> Unit,
    val onCreateAnalyzer: (CoroutineScope) -> FacesImageAnalyzer,
    val onClearMetrics: () -> Unit = {},
   )

data class CameraState(
    val controller: LifecycleCameraController,
    val isFlipped: Boolean,
    val isLandscape: Boolean,
    val toggleCamera: () -> Unit,
)


data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}


sealed interface UIRecognitionStatus {
    data class Known(
        val name: String,
        val confidence: Float,
    ) : UIRecognitionStatus

    data object Unknown : UIRecognitionStatus
}


fun List<FaceRecognitionResult>.toUiFaces(): List<UIFace> = this.map { it.toUiFace() }

fun Rectangle.toAdjustedBoundingBox(): BoundingBox = BoundingBox(
    left = x.toFloat(),
    top = y.toFloat(),
    right = (x + width).toFloat(),
    bottom = (y + height).toFloat(),
)

fun FaceRecognitionResult.toUiFace(): UIFace = UIFace(
    id = faceDetection.trackingId,
    boundingBox = faceDetection.boundingBox,
    recognitionState = recognitionStatus.toUiRecognitionStatus(),
)

fun RecognitionStatus.toUiRecognitionStatus(): UIRecognitionStatus = when (this) {
    is RecognitionStatus.Known -> UIRecognitionStatus.Known(
        name = person.name,
        confidence = confidence,
    )

    is RecognitionStatus.Unknown -> UIRecognitionStatus.Unknown
}


data class UiMetricData(
    val lastValue: Float, val average: Float, val averageFPS: Float
)

// Convert the current duration value into microseconds and cast to a Float.
// Using microseconds ensures high precision for short durations while avoiding excessive granularity like nanoseconds.
// The Float type is chosen because it's easier to animate and interpolate compared to Long or Double.
fun MetricData.toUiMetrics(): UiMetricData = UiMetricData(
    lastValue = lastValue.inWholeMicroseconds.toFloat(),
    average = average.inWholeMicroseconds.toFloat(),
    averageFPS = average.calculateFPS().toFloat()
)

fun Map<String, MetricData>.toUiMetricsMap(): Map<String, UiMetricData> = map { (key, metric) ->
    key to metric.toUiMetrics()
}.toMap()
