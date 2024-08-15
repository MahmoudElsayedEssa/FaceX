package com.example.facex.domain.usecase

import android.util.Log
import com.example.facex.di.DefaultDispatcher
import com.example.facex.di.IoDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.findRecognizedPerson
import com.example.facex.domain.repository.MLRepository
import com.example.facex.domain.repository.PersonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

@Singleton
class RecognizeFacesUseCase @Inject constructor(
    private val personRepository: PersonRepository,
    private val mlRepository: MLRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        detectedFaces: List<DetectedFace?>
    ): List<RecognizedPerson> = withContext(defaultDispatcher) {
        if (detectedFaces.isEmpty()) return@withContext emptyList()
        val persons = withContext(ioDispatcher) { personRepository.getAllPersons().first() }
        val recognizedPersons = mutableListOf<RecognizedPerson>()
        val timeTaken = measureTimeMillis {
            recognizedPersons.addAll(coroutineScope {
                detectedFaces.map { face ->
                    yield()
                    async {
                        if (face == null) return@async null
                        val embedding =
                            mlRepository.getFaceEmbedding(face.bitmap)
                        persons.findRecognizedPerson(face, embedding)
                    }
                }.awaitAll().filterNotNull()
            })
        }
        Log.d("MAMO", "Time taken to recognize faces: $timeTaken ms")
        recognizedPersons
    }
}

