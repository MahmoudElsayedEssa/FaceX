package com.example.facex.ui.screens.camera_face_recognition

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.usecase.DetectFacesUseCase
import com.example.facex.domain.usecase.RecognizeFacesUseCase
import com.example.facex.domain.usecase.RegisterPersonUseCase
import com.example.facex.domain.usecase.StopRecognitionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val registerPerson: RegisterPersonUseCase,
    private val stopRecognition: StopRecognitionUseCase,
    private val detectFacesUseCase: DetectFacesUseCase,
    private val recognizeFacesUseCase: RecognizeFacesUseCase,
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(RecognitionState())
    val stateFlow: StateFlow<RecognitionState> = _stateFlow.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    fun onAnalysis(bitmap: Bitmap, rotationDegrees: Int) {
        viewModelScope.launch {
            try {
                detectFacesUseCase(bitmap, rotationDegrees)
                    .onEach { detectedFaces ->
                        _stateFlow.update { it.copy(detectedFaces = detectedFaces) }
                    }
                    .flatMapLatest { detectedFaces ->
                        flow { emit(recognizeFacesUseCase(detectedFaces)) }
                    }
                    .collect { recognizedPersons ->
                        _stateFlow.update { it.copy(recognizedFaces = recognizedPersons) }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error during face analysis", e)
            }
        }
    }

    fun onRegisterPerson(name: String, embedding: Embedding) {
        viewModelScope.launch(Dispatchers.IO) {
            registerPerson(name = name, embedding = embedding)
        }
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
