package com.example.facex.domain.usecase

import com.example.facex.domain.repository.PersonRepository
import javax.inject.Inject

class GetPersonsUseCase @Inject constructor(private val personRepository: PersonRepository) {
    suspend operator fun invoke()
       = personRepository.getAllPersons()

}
