package com.example.facex.domain.usecase

import com.example.facex.domain.repository.MLRepository
import javax.inject.Inject

class StopRecognitionUseCase @Inject constructor(private val mlRepository: MLRepository) {

    operator fun invoke(){
       mlRepository.close()
    }
}
