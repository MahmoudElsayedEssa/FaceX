package com.example.facex.domain.usecase

import android.graphics.Bitmap
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.findRecognizedPerson
import com.example.facex.domain.repository.MLRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecognizeFacesUseCase @Inject constructor(private val mlRepository: MLRepository) {

    operator fun invoke(
        bitmap: Bitmap,
        rotationDegrees: Int,
        persons: List<Person>
    ): Flow<Pair<List<DetectedFace>, List<RecognizedPerson>?>> = flow {
        val detectedFaces = withContext(Dispatchers.Default) {
            mlRepository.recognizeFaces(bitmap, rotationDegrees).first()
        }
        val recognizedPersons = withContext(Dispatchers.Default) {
            detectedFaces.mapNotNull { persons.findRecognizedPerson(it) }
        }
        emit(detectedFaces to recognizedPersons)
    }.flowOn(Dispatchers.Default)


    companion object {
        const val CONFIDENCE_THRESHOLD = 0.4f
        private const val TAG = "RecognizeFacesUseCase"
    }
}
