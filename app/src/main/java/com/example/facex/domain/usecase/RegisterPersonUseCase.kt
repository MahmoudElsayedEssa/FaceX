package com.example.facex.domain.usecase

import com.example.facex.data.local.ml.facerecognition.FaceRecognizer
import com.example.facex.domain.repository.PersonRepository
import java.nio.ByteBuffer
import javax.inject.Inject

class RegisterPersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(
        embedding: ByteBuffer,
        name: String,
    ) {
        personRepository.savePerson(name = name, embedding = embedding)
    }


}
