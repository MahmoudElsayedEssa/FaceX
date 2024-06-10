package com.example.facex.domain.entities

data class RecognizedPerson(
    val person: Person,
    val confidence: Double
)
