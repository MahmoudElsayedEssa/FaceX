package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.helpers.CustomANNFaceRecognizerLSH
import com.example.facex.domain.usecase.DetectFacesUseCase
import com.example.facex.domain.usecase.GetFaceEmbeddingUseCase
import com.example.facex.domain.usecase.GetPersonsUseCase
import com.example.facex.domain.usecase.PerformanceTracker
import com.example.facex.domain.usecase.RecognizeFacesUseCase
import com.example.facex.domain.usecase.RegisterPersonUseCase
import com.example.facex.domain.usecase.StopRecognitionUseCase
import com.example.facex.ui.FrameData
import com.example.facex.ui.TrackedFace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val registerPerson: RegisterPersonUseCase,
    private val stopRecognition: StopRecognitionUseCase,
    private val detectFacesUseCase: DetectFacesUseCase,
    private val recognizeFacesUseCase: RecognizeFacesUseCase,
    private val getFaceEmbedding: GetFaceEmbeddingUseCase,
    private val performanceTracker: PerformanceTracker,
    private val getPersons: GetPersonsUseCase,

    ) : ViewModel() {

    private val _stateFlow = MutableStateFlow(RecognitionState())
    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()

    private var analysisJob: Job? = null

    val performanceMetrics = performanceTracker.performanceMetrics

    private val _personsFlow = MutableStateFlow<List<Person>>(emptyList())
    val personsFlow: StateFlow<List<Person>> = _personsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            getPersons().collect { persons ->
                _personsFlow.value = persons
            }
        }
    }


    fun onAnalysis(frameData: FrameData) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            try {
                supervisorScope {
                    val persons = personsFlow.first()

                    val detectedFacesDeferred = async { detectFacesUseCase(frameData) }
                    val detectedFaces = detectedFacesDeferred.await()
                    val recognizedPersonsDeferred = async {
                        val customANNFaceRecognizerLSH = CustomANNFaceRecognizerLSH(persons)
                        recognizeFacesUseCase(detectedFaces, customANNFaceRecognizerLSH)
                    }

                    launch { updateDetectedFaces(detectedFaces) }
                    launch { updateRecognizedFaces(recognizedPersonsDeferred.await()) }
                }

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
                        val updatedTrackedFace = this[id]?.copy(recognizedPerson = recognizedPerson)
                            ?: TrackedFace(
                                id = id,
                                boundingBox = recognizedPerson.detectedFace.boundingBox,
                                bitmap = recognizedPerson.detectedFace.bitmap,
                                recognizedPerson = recognizedPerson
                            )
                        if (this[id] != updatedTrackedFace) {
                            this[id] = updatedTrackedFace
                        }
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