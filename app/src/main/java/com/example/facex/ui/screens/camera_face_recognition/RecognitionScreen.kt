package com.example.facex.ui.screens.camera_face_recognition

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.facex.ui.helpers.FacesImageAnalyzer
import com.example.facex.ui.screens.camera_face_recognition.components.CameraNotchEffect
import com.example.facex.ui.screens.camera_face_recognition.components.CameraSwitchButton
import com.example.facex.ui.screens.camera_face_recognition.components.FaceDetailsDialog
import com.example.facex.ui.screens.camera_face_recognition.components.FaceRegistrationDialog
import com.example.facex.ui.screens.camera_face_recognition.components.FacesOverlayCameraPreview
import com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets.ModelControlBottomSheet
import com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets.PerformanceBottomSheet
import com.example.facex.ui.screens.camera_face_recognition.components.fabs.ExpandableFabGroup
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraRecognitionScreen(
    state: RecognitionState, actions: RecognitionActions
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val performanceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val modelControlSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)


    val cutout = LocalView.current.rootWindowInsets?.displayCutout


    val facesImageAnalyzer = remember {
        actions.onCreateAnalyzer(lifecycleOwner.lifecycleScope)
    }

    val cameraState = rememberCameraState(facesImageAnalyzer)

    DisposableEffect(facesImageAnalyzer) {
        onDispose {
            facesImageAnalyzer.cleanup()
            Log.d("MAMO", "facesImageAnalyzer.cleanup: ")
        }
    }

    Scaffold(floatingActionButton = {
        ExpandableFabGroup(
            modifier = Modifier.padding(16.dp),
            onControlModelsClick = actions.onShowModelControlBottomSheet,
            onMetricsClick = actions.onShowPerformanceBottomSheet
        )
    }

    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FacesOverlayCameraPreview(controller = cameraState.controller,
                modifier = Modifier.fillMaxSize(),
                faces = state.faces,
                isFlippedHorizontally = cameraState.isFlipped,
                isLandscape = cameraState.isLandscape,
                onFaceTap = { face ->
                    when (face.recognitionState) {
                        is UIRecognitionStatus.Known -> actions.onShowFaceDetailsDialog()
                        else -> {
                            val lastFrame = facesImageAnalyzer.getLastFrame()
                            Log.d("MAMO", "showRegistrationDialog:lastFrame$lastFrame")
                            lastFrame?.let {
                                actions.onShowRegistrationDialog(
                                    face, lastFrame, cameraState.isFlipped
                                )
                            }
                        }
                    }

                    actions.onTapFace(face)
                })


            if (cameraState.isFlipped) {

                cutout?.let {
                    CameraNotchEffect(
                        cutout = it, modifier = Modifier.fillMaxSize()
                    )
                }
            }

            CameraSwitchButton(
                onClick = { cameraState.toggleCamera.invoke() },
                modifier = Modifier
                    .safeGesturesPadding()
                    .padding(end = 16.dp)
                    .align(Alignment.TopEnd)
            )

        }

        when (state.activeDialog) {
            ActiveDialog.NONE -> {}
            ActiveDialog.REGISTRATION -> {
                FaceRegistrationDialog(faceImage = state.capturedFaceData?.image?.asImageBitmap(),
                    onDismiss = actions.onDismissDialog,
                    onRegister = { name ->
                        state.capturedFaceData?.frameData?.let { actions.onRegisterFace(it, name) }
                    })

            }

            ActiveDialog.FACE_DETAILS -> {
                state.selectedFace?.let {
                    FaceDetailsDialog(
                        face = it, onDismiss = actions.onDismissDialog
                    )
                }

            }

            ActiveDialog.PERFORMANCE -> {
                PerformanceBottomSheet(
                    averageFps = state.framesPerSecond,
                    sheetState = performanceSheetState,
                    onDismissBottomSheet = {
                        scope.launch {
                            performanceSheetState.hide()
                            actions.onDismissDialog()
                        }
                    },
                    onClearMetrics = actions.onClearMetrics
                )

            }

            ActiveDialog.MODEL_CONTROL -> {
                ModelControlBottomSheet(
                    sheetState = modelControlSheetState, onDismissDialog = actions.onDismissDialog
                )
            }
        }
    }
}


@SuppressLint("LocalContextConfigurationRead")
@Composable
private fun rememberCameraState(facesImageAnalyzer: FacesImageAnalyzer): CameraState {
    val context = LocalContext.current
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    val isLandscape = remember {
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY).build()


    val controller = remember(facesImageAnalyzer) {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            this.cameraSelector = cameraSelector

            // The detector requires the image format in NV21, while the ML model expects RGBA_8888.
            // By default, CameraX produces images in YUV_420_888 format, which would require two
            // conversions: first from YUV_420_888 to NV21 for the detector, and then to RGBA_8888
            // for the ML model. To simplify processing, we configure CameraX to directly produce
            // images in RGBA_8888 format, minimizing conversions and improving efficiency
            imageAnalysisOutputImageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888

            imageAnalysisResolutionSelector = resolutionSelector
            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            imageAnalysisImageQueueDepth = 2 // Default is 3

            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context), facesImageAnalyzer
            )

        }
    }
    return remember(controller, cameraSelector, isLandscape) {
        CameraState(controller = controller,
            isFlipped = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA,
            isLandscape = isLandscape,
            toggleCamera = {
                cameraSelector = when (cameraSelector) {
                    CameraSelector.DEFAULT_FRONT_CAMERA -> CameraSelector.DEFAULT_BACK_CAMERA
                    else -> CameraSelector.DEFAULT_FRONT_CAMERA
                }
                controller.cameraSelector = cameraSelector
            })
    }
}