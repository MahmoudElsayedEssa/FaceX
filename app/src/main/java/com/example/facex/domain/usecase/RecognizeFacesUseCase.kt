package com.example.facex.domain.usecase

import android.graphics.Bitmap
import com.example.facex.domain.cosineSimilarity
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.repository.MLRepository
import java.nio.ByteBuffer
import javax.inject.Inject

class RecognizeFacesUseCase @Inject constructor(private val mlRepository: MLRepository) {
    operator fun invoke(
        bitmap: Bitmap,
        rotationDegrees: Int,
        persons: List<Person>,
        onDetectFace: (List<DetectedFace>) -> Unit,
        onRecognizedPerson: (List<DetectedFace>, List<RecognizedPerson>?) -> Unit
    ) {
        val recognizedPersons = mutableListOf<RecognizedPerson>()

        mlRepository.detectFacesInImage(bitmap, rotationDegrees) { detectedFaces ->
            onDetectFace(detectedFaces)
            detectedFaces.map { face ->
                face.embedding?.let { persons.findRecognizedPerson(it) }.let { recognizedPerson ->
                    if (recognizedPerson != null) {
                        recognizedPersons.add(recognizedPerson)
                    }
                }
            }

        }


        onRecognizedPerson(emptyList(), recognizedPersons)
    }

    private fun List<Person>.findRecognizedPerson(embedding: ByteBuffer): RecognizedPerson? {
        return mapNotNull { person ->
            val similarity = cosineSimilarity(embedding, person.embedding)
            if (similarity >= CONFIDENCE_THRESHOLD) RecognizedPerson(person, similarity)
            else null
        }.maxByOrNull { it.confidence }

    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private const val TAG = "RecognizeFacesUseCase"
    }
}
