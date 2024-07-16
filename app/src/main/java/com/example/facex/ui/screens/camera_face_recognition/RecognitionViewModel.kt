package com.example.facex.ui.screens.camera_face_recognition

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.usecase.RegisterPersonUseCase
import com.example.facex.domain.usecase.StopRecognitionUseCase
import com.example.facex.domain.usecase.camera.BindCameraUseCase
import com.example.facex.domain.usecase.camera.GetLinearZoomUseCase
import com.example.facex.domain.usecase.camera.GetZoomRatioUseCase
import com.example.facex.domain.usecase.camera.SetCameraAnalyzerUseCase
import com.example.facex.domain.usecase.camera.SetLinearZoomUseCase
import com.example.facex.domain.usecase.camera.SetZoomRatioUseCase
import com.example.facex.domain.usecase.camera.SwitchCameraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val bindCamera: BindCameraUseCase,
    private val setCameraAnalyzer: SetCameraAnalyzerUseCase,
    private val registerPerson: RegisterPersonUseCase,
    private val stopRecognition: StopRecognitionUseCase,
    private val switchCamera: SwitchCameraUseCase,
    private val setLinearZoom: SetLinearZoomUseCase,
    private val setZoomRatio: SetZoomRatioUseCase,
    private val getLinerZoom: GetLinearZoomUseCase,
    private val getZoomRatio: GetZoomRatioUseCase
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(RecognitionState())
    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()

    init {
        initializeCameraAnalyzer()
    }

    private fun initializeCameraAnalyzer() {
        viewModelScope.launch {
            setCameraAnalyzer { (detectedFaces, recognizedPersons) ->
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

    fun onRegisterPerson(name: String, embedding: Embedding) {
        viewModelScope.launch(Dispatchers.IO) {
            registerPerson(name = name, embedding = embedding)
        }
    }

    fun startCamera(lifecycleOwner: LifecycleOwner) {
        Log.d(TAG, "onStartCamera: ")
        viewModelScope.launch {
            bindCamera(lifecycleOwner).collect { cameraState ->
                Log.d(TAG, "onStartCamera: cameraState$cameraState")
                _stateFlow.update {
                    it.copy(cameraState = cameraState)
                }
            }
        }
    }

    fun onSwitchCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            switchCamera(lifecycleOwner)
        }
    }

    fun onLinearZoom(zoom: Float) {
        setLinearZoom(zoom)
    }

    fun onZoomRatio(ratio: Float) {
        setZoomRatio(ratio)
    }

    fun getLinearZoom(): StateFlow<Float> = getLinerZoom()

    fun onZoomRatio(): StateFlow<Float> = getZoomRatio()

    fun onHandleTapToFocus(x: Float, y: Float) {
//        handleTapToFocus(x, y)
    }

    fun onStopRecognition() {
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
