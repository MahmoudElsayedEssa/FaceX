package com.example.facex.domain.usecase

import android.graphics.Bitmap
import com.example.facex.di.DefaultDispatcher
import com.example.facex.di.IoDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.findRecognizedPerson
import com.example.facex.domain.repository.MLRepository
import com.example.facex.domain.repository.PersonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecognizeFacesUseCase @Inject constructor(
    private val faceRecognizer: MLRepository,
    private val personRepository: PersonRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher

) {
    suspend operator fun invoke(bitmap: Bitmap, rotationDegrees: Int):
            Pair<List<DetectedFace>, List<RecognizedPerson>> = runBlocking(defaultDispatcher) {

        val persons = withContext(ioDispatcher) { personRepository.getAllPersons().first() }
        val detectedFaces = faceRecognizer.recognizeFaces(bitmap, rotationDegrees)
        val recognizedPersons = detectedFaces.mapNotNull { persons.findRecognizedPerson(it) }

        detectedFaces to recognizedPersons
    }

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.58f
        private const val TAG = "RecognizeFacesUseCase"
    }
}