package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.usecase.DetectFacesUseCase
import com.example.facex.domain.usecase.GetFaceEmbeddingUseCase
import com.example.facex.domain.usecase.PerformanceTracker
import com.example.facex.domain.usecase.RecognizeFacesUseCase
import com.example.facex.domain.usecase.RegisterPersonUseCase
import com.example.facex.domain.usecase.StopRecognitionUseCase
import com.example.facex.ui.FrameData
import com.example.facex.ui.TrackedFace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureNanoTime

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val registerPerson: RegisterPersonUseCase,
    private val stopRecognition: StopRecognitionUseCase,
    private val detectFacesUseCase: DetectFacesUseCase,
    private val recognizeFacesUseCase: RecognizeFacesUseCase,
    private val getFaceEmbedding: GetFaceEmbeddingUseCase,
    val performanceTracker: PerformanceTracker
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(RecognitionState())
    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()

    private var analysisJob: Job? = null

    val performanceMetrics = performanceTracker.performanceMetrics


//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun onAnalysis(frameData: FrameData) {
//        analysisJob?.cancel() // Cancel previous job if it's still running
//        analysisJob = viewModelScope.launch {
//            try {
//                val totalTime = measureNanoTime {
//                    detectFacesUseCase(frameData)
//                        .flatMapLatest { detectedFaces ->
//                            updateDetectedFaces(detectedFaces)
//                            flowOf(recognizeFacesUseCase(detectedFaces))
//                        }
//                        .collect { recognizedPersons ->
//                            updateRecognizedFaces(recognizedPersons)
//                        }
//                }
//                performanceTracker.updateMetric(PerformanceTracker.TOTAL_ANALYSIS_TIME, totalTime)
//            } catch (e: Exception) {
//                handleException(e)
//            }
//        }
//    }


//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun onAnalysis(frameData: FrameData) {
//        analysisJob?.cancel() // Cancel previous job if it's still running
//        analysisJob = viewModelScope.launch {
//            try {
//                val totalTime = measureNanoTime {
//                    val detectedFaces = async { detectFacesUseCase(frameData) }.await()
//                    updateDetectedFaces(detectedFaces)
//                    val recognizedPersons = async { recognizeFacesUseCase(detectedFaces) }.await()
//                    updateRecognizedFaces(recognizedPersons)
//                }
//                performanceTracker.updateMetric(PerformanceTracker.TOTAL_ANALYSIS_TIME, totalTime)
//            } catch (e: Exception) {
//                handleException(e)
//            }
//        }
//    }



    @OptIn(ExperimentalCoroutinesApi::class)
    fun onAnalysis(frameData: FrameData) {
        analysisJob?.cancel() // Cancel previous job if it's still running
        analysisJob = viewModelScope.launch {
            try {
                val totalTime = measureNanoTime {
                    // Run detection and recognition in parallel
                    val (detectedFaces, recognizedPersons) = coroutineScope {
                        val detectedFacesDeferred = async { detectFacesUseCase(frameData) }
                        val recognizedPersonsDeferred = async {
                            detectedFacesDeferred.await().let { recognizeFacesUseCase(it) }
                        }

                        Pair(detectedFacesDeferred.await(), recognizedPersonsDeferred.await())
                    }

                    // Update results in parallel
                    launch { updateDetectedFaces(detectedFaces) }
                    launch { updateRecognizedFaces(recognizedPersons) }
                }
                performanceTracker.updateMetric(PerformanceTracker.TOTAL_ANALYSIS_TIME, totalTime)
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                Log.d("performanceTracker", "invoke:TOTAL_ANALYSIS_TIME:${formatter.format(totalTime)} ")
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }


    private fun updateDetectedFaces(detectedFaces: List<DetectedFace?>) {
        _stateFlow.update { currentState ->
            currentState.copy(
                trackedFaces = detectedFaces.filterNotNull().associate { face ->
                    val id = face.trackedId ?: face.hashCode()
                    id to TrackedFace(id, face.boundingBox, face.bitmap)
                }
            )
        }
    }

    private fun updateRecognizedFaces(recognizedPersons: List<RecognizedPerson>) {
        _stateFlow.update { currentState ->
            currentState.copy(
                trackedFaces = currentState.trackedFaces.toMutableMap().apply {
                    recognizedPersons.forEach { recognizedPerson ->
                        val id = recognizedPerson.detectedFace.trackedId
                            ?: recognizedPerson.detectedFace.hashCode()
                        this[id] = this[id]?.copy(recognizedPerson = recognizedPerson)
                            ?: TrackedFace(
                                id = id,
                                boundingBox = recognizedPerson.detectedFace.boundingBox,
                                bitmap = recognizedPerson.detectedFace.bitmap,
                                recognizedPerson = recognizedPerson
                            )
                    }
                }
            )
        }
    }

    private fun handleException(e: Exception) {
        when (e) {
            is CancellationException -> Log.d(TAG, "Face analysis cancelled: ${e.message}")
            else -> {
                Log.e(TAG, "Error during face analysis", e)
                clearState()
            }
        }
    }

    private fun clearState() {
        _stateFlow.update { it.copy(trackedFaces = emptyMap()) }
    }


    fun onRegisterPerson(name: String, faceBitmap: Bitmap) {
        viewModelScope.launch {
            val embedding = async { getFaceEmbedding(faceBitmap) }.await()
            registerPerson(name = name, embedding = embedding)
        }
    }

    fun onStopRecognition() {
        analysisJob?.cancel()
        analysisJob = null
        stopRecognition()
    }

    fun clearPerformanceMetrics() {
        performanceTracker.clear()
    }

    override fun onCleared() {
        super.onCleared()
        onStopRecognition()
        clearPerformanceMetrics()
    }

    companion object {
        private const val TAG = "RecognitionViewModel"
    }
}