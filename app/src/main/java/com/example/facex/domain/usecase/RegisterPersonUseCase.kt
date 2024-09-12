package com.example.facex.domain.usecase

import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.repository.PersonRepository
import javax.inject.Inject

class RegisterPersonUseCase @Inject constructor(private val personRepository: PersonRepository) {
    suspend operator fun invoke(embedding: Embedding, name: String) {
        personRepository.savePerson(name = name, embedding = embedding)
    }
}
