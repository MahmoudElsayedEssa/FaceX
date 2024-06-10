package com.example.facex.domain.usecase

import com.example.facex.domain.cosineSimilarity
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.repository.PersonRepository
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.sqrt

class RecognizePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(embedding: ByteBuffer): RecognizedPerson? {
        val persons = personRepository.getAllPersons()
        return persons
            .map { person ->
                person to cosineSimilarity(embedding, person.embedding)
            }.maxByOrNull { it.second }?.let { pair ->
                RecognizedPerson(person = pair.first, confidence = pair.second)
            }

    }



}
