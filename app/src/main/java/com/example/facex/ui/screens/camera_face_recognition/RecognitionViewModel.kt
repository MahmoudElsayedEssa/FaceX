package com.example.facex.ui.screens.camera_face_recognition

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.usecase.GetPersonsUseCase
import com.example.facex.domain.usecase.SetCameraAnalyzerUseCase
import com.example.facex.domain.usecase.StartCameraUseCase
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
    private val startCamera: StartCameraUseCase,
    private val setCameraAnalyzer: SetCameraAnalyzerUseCase,
    private val getPersons: GetPersonsUseCase,
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<RecognitionState> =
        MutableStateFlow(RecognitionState())

    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()

    private val personStateFlow: MutableStateFlow<List<Person>> =
        MutableStateFlow(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            personStateFlow.update { getPersons() }
        }
        Log.d(TAG, "init: setCameraAnalyzer")
        setCameraAnalyzer(
            persons = personStateFlow.value,
            onDetectFace = ::onDetectFace,
            onRecognizedPerson = ::onRecognizedPerson
        )

        initializeRecognition()
    }

    private fun initializeRecognition() {
        viewModelScope.launch {
            personStateFlow.collect { persons ->
                setCameraAnalyzer(
                    persons = persons,
                    onDetectFace = ::onDetectFace,
                    onRecognizedPerson = ::onRecognizedPerson
                )
            }

        }
    }

    /*
    *  onDetectFace: (List<DetectedFace>) -> Unit,
            onRecognizedPerson: (List<DetectedFace>, List<RecognizedPerson>?) -> Unit
    * */
    private fun onDetectFace(detectedFaces: List<DetectedFace>) {
        Log.d(TAG, "onDetectFace: rect $detectedFaces")
        _stateFlow.update {
            it.copy(
                detectedFaces = detectedFaces
            )
        }

    }

    private fun onRecognizedPerson(
        detectedFaces: List<DetectedFace>,
        recognizedPersons: List<RecognizedPerson>?
    ) {
        Log.d(
            TAG,
            "onRecognizedPerson: detectedFace $detectedFaces, recognizedPerson $recognizedPersons"
        )
        _stateFlow.update {
            it.copy(
//                recognizedFaces = detectedFaces
            )
        }
    }

    fun onStartCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        Log.d(TAG, "onStartCamera:")
        startCamera(previewView, lifecycleOwner)
    }

    companion object {
        private const val TAG = "RecognitionViewModel"
    }


}