package com.example.facex.domain.entities

sealed interface RecognitionStatus {
    data class Known(
        val person: Person,
        val confidence: Float,
    ) : RecognitionStatus

    data object Unknown : RecognitionStatus
}
