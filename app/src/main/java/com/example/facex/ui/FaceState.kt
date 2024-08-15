package com.example.facex.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FaceState {
    var isRecognized by mutableStateOf(false)
    var displayName by mutableStateOf("")
    var confidence by mutableDoubleStateOf(0.0)
    private var recognitionCount = 0

    fun update(newIsRecognized: Boolean, newDisplayName: String, newConfidence: Double) {
        if (newIsRecognized) {
            recognitionCount++
            if (recognitionCount >= RECOGNITION_THRESHOLD) {
                isRecognized = true
                displayName = newDisplayName
                confidence = newConfidence
            }
        } else {
            recognitionCount = 0
            if (isRecognized) {
                // Keep the previous recognition for a few more frames
                recognitionCount = RECOGNITION_THRESHOLD - 1
            }
        }
    }

    companion object {

        private const val RECOGNITION_THRESHOLD = 3
    }
}
