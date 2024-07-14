package com.example.facex.domain

import android.util.Log
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.usecase.RecognizeFacesUseCase
import com.example.facex.domain.usecase.RecognizeFacesUseCase.Companion.CONFIDENCE_THRESHOLD
import java.nio.ByteBuffer
import kotlin.math.sqrt


fun cosineSimilarity(embeddingA: ByteBuffer, embeddingB: ByteBuffer): Double {
    require(embeddingA.capacity() == embeddingB.capacity()) { "Buffers must have the same dimensions" }

    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0

    embeddingA.rewind()
    embeddingB.rewind()

    while (embeddingA.hasRemaining() && embeddingB.hasRemaining()) {
        val valueA = embeddingA.float
        val valueB = embeddingB.float

        dotProduct += valueA * valueB
        normA += valueA * valueA
        normB += valueB * valueB
    }

    embeddingA.rewind()
    embeddingB.rewind()

    return dotProduct / (sqrt(normA) * sqrt(normB))
}

fun List<Person>.findRecognizedPerson(detectedFace: DetectedFace): RecognizedPerson? {
    return this.mapNotNull { person ->
        cosineSimilarity(detectedFace.embedding, person.embedding).takeIf {
            it >= CONFIDENCE_THRESHOLD
        }?.let { confidence ->
            RecognizedPerson(person, confidence, detectedFace).also {
                Log.d("findRecognizedPerson", "findRecognizedPerson: confidence: $confidence")
            }
        }
    }.maxByOrNull { it.confidence }
}
