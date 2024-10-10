package com.example.facex.ui.screens.camera_face_recognition

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.PerformanceTracker
import com.example.facex.domain.entities.Person
import com.example.facex.domain.helpers.FaceRecognizer
import com.example.facex.domain.usecase.DetectFacesUseCase
import com.example.facex.domain.usecase.GetFaceEmbeddingUseCase
import com.example.facex.domain.usecase.GetPersonsUseCase
import com.example.facex.domain.usecase.RecognizeFacesUseCase
import com.example.facex.domain.usecase.RegisterPersonUseCase
import com.example.facex.domain.usecase.StopRecognitionUseCase
import com.example.facex.ui.FrameData
import com.example.facex.ui.TrackedFace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class)
@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val registerPerson: RegisterPersonUseCase,
    private val stopRecognition: StopRecognitionUseCase,
    private val detectFacesUseCase: DetectFacesUseCase,
    private val recognizeFacesUseCase: RecognizeFacesUseCase,
    private val getFaceEmbedding: GetFaceEmbeddingUseCase,
    private val getPersons: GetPersonsUseCase,
    val performanceTracker: PerformanceTracker,
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(RecognitionState())
    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()

    private var analysisJob: Job? = null

    val performanceMetrics = performanceTracker.performanceMetrics

    private val recognizer = FaceRecognizer(useLSH = false)
    private val _personsFlow = MutableStateFlow<List<Person>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            getPersons()
                .distinctUntilChanged()
                .collect { persons ->
                    updatePersons(persons)
                }
        }
    }

    private suspend fun updatePersons(newPersons: List<Person>) {
        val oldPersons = _personsFlow.value
        val (added, removed) = diffPersons(oldPersons, newPersons)

        added.forEach { recognizer.addPerson(it) }
        removed.forEach { recognizer.removePerson(it) }

        _personsFlow.value = newPersons
    }

    private fun diffPersons(
        old: List<Person>,
        new: List<Person>
    ): Pair<List<Person>, List<Person>> {
        val added = new.filter { it !in old }
        val removed = old.filter { it !in new }
        return Pair(added, removed)
    }

    fun onAnalysis(frameData: FrameData) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                supervisorScope {
                    val updatedTrackedFaces =
                        performanceTracker.measureSuspendPerformance(PerformanceTracker.MetricKey.TOTAL_PROCESSING_TIME) {
                            val detectedFaces = detectFacesUseCase(frameData)
                            val recognizedPersons = recognizeFacesUseCase(detectedFaces, recognizer)
                            detectedFaces.map { df ->
                                df.let {
                                    val id = it.trackedId ?: it.hashCode()
                                    val recognizedPerson = recognizedPersons
                                        .find { rp -> rp.detectedFace.trackedId == id }

                                    TrackedFace(
                                        id, it.boundingBox, it.imageByteBuffer, recognizedPerson
                                    )
                                }
                            }
                        }
                    launch {
                        _stateFlow.update {
                            it.copy(trackedFaces = updatedTrackedFaces)
                        }
                    }
                }

            } catch (e: Exception) {
                handleException(e)
            }
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
        _stateFlow.update { it.copy(trackedFaces = emptyList()) }
    }


    fun onRegisterPerson(name: String, faceByteBuffer: ByteBuffer, width: Int, height: Int) {
        viewModelScope.launch {
            val embedding = async { getFaceEmbedding(faceByteBuffer, width, height) }.await()
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