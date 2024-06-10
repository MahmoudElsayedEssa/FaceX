package com.example.facex.domain.usecase

import com.example.facex.domain.cosineSimilarity
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import java.nio.ByteBuffer
import javax.inject.Inject

class RecognizePersonUseCase @Inject constructor() {
    operator fun invoke(embedding: ByteBuffer, persons: List<Person>): RecognizedPerson? {
        return persons
            .map { person ->
                person to cosineSimilarity(embedding, person.embedding)
            }.maxByOrNull { it.second }?.let { pair ->
                RecognizedPerson(person = pair.first, confidence = pair.second)
            }

    }


}
