package com.example.facex.ui.screens.capturefacetorecognize

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class CaptureFaceToRecognizeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<CaptureFaceToRecognizeState> =
        MutableStateFlow(CaptureFaceToRecognizeState())

    val stateFlow: StateFlow<CaptureFaceToRecognizeState> = _stateFlow.asStateFlow()


}