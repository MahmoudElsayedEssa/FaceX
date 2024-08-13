package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.usecase.DetectFacesUseCase
import com.example.facex.domain.usecase.RecognizeFacesUseCase
import com.example.facex.domain.usecase.RegisterPersonUseCase
import com.example.facex.domain.usecase.StopRecognitionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val registerPerson: RegisterPersonUseCase,
    private val stopRecognition: StopRecognitionUseCase,
    private val detectFacesUseCase: DetectFacesUseCase,
    private val recognizeFacesUseCase: RecognizeFacesUseCase,
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(RecognitionState())
    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()

    private var analysisJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onAnalysis(bitmap: Bitmap, rotationDegrees: Int) {
        analysisJob = viewModelScope.launch {
            try {
                var recognitionDeffer: Deferred<List<RecognizedPerson>>? = null
                detectFacesUseCase(bitmap, rotationDegrees)
                    .onEach { detectedFaces ->
                        _stateFlow.update { it.copy(detectedFaces = detectedFaces) }
                        if (detectedFaces.isEmpty()) {
                            _stateFlow.update { it.copy(recognizedFaces = emptyList()) }
                            recognitionDeffer?.cancel()
                            throw CancellationException("No faces detected")
                        }
                    }
                    .flatMapLatest { detectedFaces ->
                        flow {
                            yield()
                            recognitionDeffer = async { recognizeFacesUseCase(detectedFaces) }
                            emit(recognitionDeffer)
                        }
                    }
                    .collect { recognizedPersons ->
                        _stateFlow.update {
                            recognizedPersons?.await()
                                ?.let { it1 -> it.copy(recognizedFaces = it1) }!!
                        }
                    }
            } catch (e: CancellationException) {
                // Log if needed, but don't rethrow as this is an expected cancellation
                Log.d(TAG, "Face analysis cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error during face analysis", e)
                _stateFlow.update {
                    it.copy(
                        detectedFaces = emptyList(),
                        recognizedFaces = emptyList()
                    )
                }
            }
        }
    }

    fun onRegisterPerson(name: String, embedding: Embedding) {
        viewModelScope.launch(Dispatchers.IO) {
            registerPerson(name = name, embedding = embedding)
        }
    }

    fun onStopRecognition() {
        analysisJob?.cancel()
        analysisJob = null
        stopRecognition()
        _stateFlow.update { it.copy(detectedFaces = emptyList(), recognizedFaces = emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        onStopRecognition()
    }

    companion object {
        private const val TAG = "RecognitionViewModel"
    }
}