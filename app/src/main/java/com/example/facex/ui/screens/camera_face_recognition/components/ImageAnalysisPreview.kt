import android.content.res.Configuration
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.facex.domain.entities.PerformanceTracker
import com.example.facex.ui.TrackedFace
import com.example.facex.ui.components.CameraPreview
import com.example.facex.ui.helpers.FacesImageAnalyzer
import com.example.facex.ui.screens.camera_face_recognition.RecognitionActions
import com.example.facex.ui.screens.camera_face_recognition.RecognitionState
import com.example.facex.ui.screens.camera_face_recognition.RecognitionViewModel
import com.example.facex.ui.screens.camera_face_recognition.components.FaceGraphic
import com.example.facex.ui.screens.camera_face_recognition.components.GraphicOverlay
import com.example.facex.ui.screens.camera_face_recognition.components.PerformanceMetricsDisplay
import com.example.facex.ui.screens.camera_face_recognition.components.combinedPointerInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageAnalysisPreview(
    state: RecognitionState,
    actions: RecognitionActions,
    onFaceTapped: (TrackedFace) -> Unit,
    modifier: Modifier = Modifier
) {
    val performanceTracker: PerformanceTracker =
        hiltViewModel<RecognitionViewModel>().performanceTracker

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var previewSize by remember { mutableStateOf(Size(0f, 0f)) }

    val orientation = LocalConfiguration.current.orientation
    val isFrontCamera = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    val analyzer = remember {
        FacesImageAnalyzer(
            lifecycleOwner.lifecycleScope,
            actions.onAnalysis,
            performanceTracker
        )
    }
    val desiredSize = android.util.Size(480, 360)
    val analysisSize = Size(480f, 360f)
    val strategy =
        ResolutionStrategy(desiredSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
    var aspectRatioStrategy =
        AspectRatioStrategy(AspectRatio.RATIO_4_3, AspectRatioStrategy.FALLBACK_RULE_NONE)

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                analyzer
            )
//            setImageAnalysisResolutionSelector(
//                ResolutionSelector.Builder()
//                    .setAspectRatioStrategy(aspectRatioStrategy)
//                    .setResolutionStrategy(strategy)
//                    .setResolutionFilter(CustomResolutionFilter())
//                    .build()
//            )
//            // Set up resolution selector for preview
//            setPreviewResolutionSelector(
//                ResolutionSelector.Builder()
//                    .setAspectRatioStrategy(aspectRatioStrategy)
//                    .setResolutionStrategy(strategy) // Custom resolution strategy
//                    .setResolutionFilter(CustomResolutionFilter()) // Custom resolution filter
//                    .build()
//            )
            cameraSelector = cameraSelector
        }
    }
    val zoomState = controller.zoomState
    var previewView = remember { PreviewView(context) }

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            BottomSheetContent(
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedPointerInput(
                    trackedFaces = state.trackedFaces.toList(),
                    analysisSize = analysisSize,
                    previewSize = previewSize,
                    isLandscape = isLandscape,
                    isFrontCamera = isFrontCamera,
//                    onFaceTapped = onFaceTapped,
                    zoom = zoomState,
                    previewView = previewView,
                    onZoomChange = { newZoom ->
                        controller.cameraControl?.setZoomRatio(newZoom)
                    },
                    onFocusTap = {
                        controller.cameraControl?.startFocusAndMetering(it)

                    }
                )
        ) {
            CameraPreview(
                controller = controller,
                modifier = Modifier
//                    .width(1280.dp)
//                    .height(720.dp)
                    .fillMaxSize(),
                onUpdatePreviewView = { size, previewVieww ->
                    previewSize = Size(size.width, size.height)
                    previewView = previewVieww
                }
            )

//            FacesOverlay(
//                trackedFaces = state.trackedFaces.values.toList(),
//                cameraSelector = cameraSelector,
//                analysisSize = analysisSize,
//                previewSize = previewSize,
//                onFaceTapped = onFaceTapped,
//                modifier = Modifier.fillMaxSize()
//            )
            val isImageFlipped = isFrontCamera

            GraphicOverlayView(state.trackedFaces.toList(), isImageFlipped, onFaceTapped)

            IconButton(
                onClick = {
                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else CameraSelector.DEFAULT_BACK_CAMERA
                    controller.cameraSelector = cameraSelector
                },
                modifier = Modifier
                    .offset(16.dp, 16.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Switch camera"
                )
            }
        }
    }
}

@Composable
fun BottomSheetContent(onDismiss: () -> Unit) {
    PerformanceMetricsDisplay()
}


@Composable
fun GraphicOverlayView(
    trackedFaces: List<TrackedFace>,
    isImageFlipped: Boolean,
    onFaceTapped: (TrackedFace) -> Unit
) {
    AndroidView(
        factory = { context ->
            GraphicOverlay(context, null).apply {
                // Add face graphics when the view is created
                setImageSourceInfo(480, 360, isImageFlipped)
                setOnFaceTappedListener(onFaceTapped)

                trackedFaces.forEach { trackedFace ->
                    add(
                        FaceGraphic(this, trackedFace)
                    )
                }
            }
        },
        update = { overlay ->
            overlay.clear()
            overlay.setImageSourceInfo(480, 360, isImageFlipped)

            trackedFaces.forEach { trackedFace ->
                overlay.add(
                    FaceGraphic(overlay, trackedFace)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
    )

}
