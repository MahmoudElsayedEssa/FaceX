import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.facex.ui.screens.camera_face_recognition.RecognitionActions
import com.example.facex.ui.screens.camera_face_recognition.RecognitionState
import com.example.facex.ui.screens.camera_face_recognition.components.CameraPreview
import com.example.facex.ui.screens.camera_face_recognition.components.FacesImageAnalyzer
import com.example.facex.ui.screens.camera_face_recognition.components.FacesOverlay

@Composable
fun ImageAnalysisPreview(
    state: RecognitionState,
    actions: RecognitionActions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifeTimeScope = LocalLifecycleOwner.current.lifecycleScope
    var cameraSelector by remember { mutableStateOf(DEFAULT_FRONT_CAMERA) }
    val analyzer = remember {
        FacesImageAnalyzer(lifeTimeScope, actions.onAnalysis)
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                analyzer
            )

            this.cameraSelector = cameraSelector
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        CameraPreview(
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
        )
        IconButton(
            onClick = {
                controller.cameraSelector =
                    if (controller.cameraSelector == DEFAULT_BACK_CAMERA) {
                        DEFAULT_FRONT_CAMERA
                    } else DEFAULT_BACK_CAMERA

                cameraSelector = controller.cameraSelector
            },
            modifier = Modifier
                .offset(16.dp, 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Switch camera"
            )
        }

        FacesOverlay(
            state.detectedFaces,
            state.recognizedFaces,
            cameraSelector = cameraSelector
        )

    }


}
