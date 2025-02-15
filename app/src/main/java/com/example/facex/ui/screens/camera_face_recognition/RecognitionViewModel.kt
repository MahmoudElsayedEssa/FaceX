package com.example.facex.ui.screens.camera_face_recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.helpers.FramePool
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.performancetracking.PerformanceTracker
import com.example.facex.domain.performancetracking.PerformanceTrackingKeys.TOTAL_TIME
import com.example.facex.domain.repository.PersonRepository
import com.example.facex.domain.usecase.FaceProcessingOrchestrator
import com.example.facex.domain.usecase.GetFaceEmbeddingUseCase
import com.example.facex.ui.helpers.AnalysisConfig
import com.example.facex.ui.helpers.FacesImageAnalyzer
import com.example.facex.ui.utils.cropFace
import com.example.facex.ui.utils.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val getEmbedding: GetFaceEmbeddingUseCase,
    private val processFaces: FaceProcessingOrchestrator,
    private val personStorage: PersonRepository,
    private val logger: Logger,
    private val framePool: FramePool,
    private val faceDetector: FaceProcessorFacade,
    val performanceTracker: PerformanceTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(RecognitionState())
    val state = _state.asStateFlow()


    init {
        logger.tag = "RecognitionViewModel"
        observePerformanceMetrics()
    }

    private fun observePerformanceMetrics() {
        viewModelScope.launch {
            performanceTracker.metricsFlow.collect {
                val framesPerSecond = it.metricDataMap()[TOTAL_TIME]?.averageFps
                updateState { copy(framesPerSecond = framesPerSecond ?: 0) }
            }
        }
    }

    private var analyzer: FacesImageAnalyzer? = null
    fun createAnalyzer(lifecycleScope: CoroutineScope): FacesImageAnalyzer {
        analyzer?.cleanup()
        return FacesImageAnalyzer(
            lifecycleScope = lifecycleScope,
            performanceTracker = performanceTracker,
            onAnalyze = ::startFrameProcessing,
            logger = logger,
            config = AnalysisConfig(
                maxQueuedFrames = 2,
                threadPoolSize = 1
            ),
            framePool = framePool,

            ).also {
            analyzer = it
        }
    }

    private fun startFrameProcessing(frameResult: Result<Frame>) {
        viewModelScope.launch(Dispatchers.Default) {
            frameResult.onSuccess { frame ->
                performanceTracker.suspendTrack(TOTAL_TIME) {
                    processFaces(frame)
                }.onSuccess { faces ->
                    updateState { copy(faces = faces.toUiFaces()) }
                }.onFailure { error ->
                    handleAnalysisError(error)
                }
            }.onFailure { error ->
                handleAnalysisError(error)
            }
        }
    }

    fun registerFace(frameWithFace: Frame, personName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            getEmbedding(frameWithFace)
                .onSuccess { faceEmbedding ->
                    personStorage.registerPerson(personName, faceEmbedding)
                    dismissDialog()
                }.onFailure { registrationError ->
                    logger.logError("Registration failed for $personName", registrationError)
                }
        }
    }

    fun onTapFace(selectedFace: UIFace) {
        updateState { copy(selectedFace = selectedFace) }
    }

    fun dismissDialog() {
        updateState { copy(activeDialog = ActiveDialog.NONE) }
    }


    fun showPerformanceSheet() {
        updateState { copy(activeDialog = ActiveDialog.PERFORMANCE) }
    }


    fun onShowModelControlBottomSheet() {
        updateState { copy(activeDialog = ActiveDialog.MODEL_CONTROL) }
    }


    fun showRegistrationDialog(
        selectedFace: UIFace, frameToRegister: Frame, isFlipped: Boolean
    ) {
        frameToRegister.let { frameData ->
            viewModelScope.launch(Dispatchers.Default) {
                val originalBitmap = frameData.alignRotation().toBitmap()

                val capturedFaceBitmap = originalBitmap.cropFace(selectedFace.boundingBox)

                val capturedFaceFrameData = CapturedFaceData(
                    image = capturedFaceBitmap,
                    frameData = frameData.toGrayscale().rotate(frameData.rotationDegrees)
                        .crop(selectedFace.boundingBox)
                )
                updateState {
                    copy(
                        activeDialog = ActiveDialog.REGISTRATION,
                        capturedFaceData = capturedFaceFrameData
                    )
                }
            }
        }
    }

    fun showFaceDetailsDialog() {
        updateState { copy(activeDialog = ActiveDialog.FACE_DETAILS) }
    }

    fun onClearMetrics() {
        performanceTracker.clear()
    }

    fun cleanup() {
        clearRecognitionState()
        performanceTracker.clear()
    }


    private fun clearRecognitionState() {
        updateState { copy(faces = emptyList()) }
    }

    private fun updateState(stateModifier: RecognitionState.() -> RecognitionState) {
        _state.update(stateModifier)
    }


    private fun handleAnalysisError(registrationError: Throwable) {
        when (registrationError) {
            is OutOfMemoryError -> {
                logger.logError("Out of memory", registrationError)
                cleanup()
            }

            is CancellationException -> {
                logger.logError("Analysis cancelled", registrationError)
            }

            else -> {
                logger.logError("Analysis failed", registrationError)
                clearRecognitionState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
        viewModelScope.launch {
            faceDetector.close()
            analyzer?.cleanup()
            framePool.cleanup()
        }
    }
}


