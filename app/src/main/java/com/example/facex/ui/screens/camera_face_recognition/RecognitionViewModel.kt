package com.example.facex.ui.screens.camera_face_recognition

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.usecase.BindCameraUseCase
import com.example.facex.domain.usecase.RegisterPersonUseCase
import com.example.facex.domain.usecase.SetCameraAnalyzerUseCase
import com.example.facex.domain.usecase.StopRecognitionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import javax.inject.Inject

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val bindCamera: BindCameraUseCase,
    private val setCameraAnalyzer: SetCameraAnalyzerUseCase,
    private val registerPerson: RegisterPersonUseCase,
    private val stopRecognition: StopRecognitionUseCase
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(RecognitionState())
    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()

    init {
        initializeCameraAnalyzer()
    }

    private fun initializeCameraAnalyzer() {
        viewModelScope.launch {
            setCameraAnalyzer(
                onRecognizeFaces = ::onRecognizeFaces
            )
        }
    }

    private fun onRecognizeFaces(
        recognizedFaces: Flow<Pair<List<DetectedFace>, List<RecognizedPerson>?>>
    ) {
        viewModelScope.launch {
            recognizedFaces.collect { (detectedFaces, recognizedPersons) ->
                onDetectFace(detectedFaces)
                onRecognizedPerson(recognizedPersons)
            }
        }
    }

    private fun onDetectFace(detectedFaces: List<DetectedFace>) {
        _stateFlow.update {
            it.copy(detectedFaces = detectedFaces)
        }
    }

    private fun onRecognizedPerson(recognizedPersons: List<RecognizedPerson>?) {
        recognizedPersons?.let {
            _stateFlow.update { state ->
                state.copy(recognizedFaces = it)
            }
        }
    }

    fun onRegisterPerson(name: String, embedding: ByteBuffer) {
        viewModelScope.launch(Dispatchers.IO) {
            registerPerson(name = name, embedding = embedding)
        }
    }

    fun onStartCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        bindCamera(previewView, lifecycleOwner)
    }

    fun onStopRecognition(){
        stopRecognition()
    }



    override fun onCleared() {
        super.onCleared()
        onStopRecognition()
    }

    companion object {
        private const val TAG = "RecognitionViewModel"
    }
}
