package com.example.facex.domain.usecase

import android.util.Log
import com.example.facex.di.DefaultDispatcher
import com.example.facex.di.IoDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
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
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureNanoTime

@Singleton
class RecognizeFacesUseCase @Inject constructor(
    private val personRepository: PersonRepository,
    private val mlRepository: MLRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private var cachedPersons: List<Person> = emptyList()

    private suspend fun refreshCache() = withContext(ioDispatcher) {
        cachedPersons = personRepository.getAllPersons().first()
    }

    suspend operator fun invoke(detectedFaces: List<DetectedFace?>): List<RecognizedPerson> =
        withContext(defaultDispatcher) {
            if (detectedFaces.isEmpty()) return@withContext emptyList()

            if (cachedPersons.isEmpty()) {
                refreshCache()
            }

            val recognizedPersons = mutableListOf<RecognizedPerson>()
            val timeTaken = measureNanoTime {
                recognizedPersons.addAll(coroutineScope {
                    detectedFaces.map { face ->
                        yield()
                        async {
                            if (face == null) return@async null
                            val embedding = mlRepository.getFaceEmbedding(face.bitmap)
                            cachedPersons.findRecognizedPerson(face, embedding)
                        }
                    }.awaitAll().filterNotNull()
                })
            }

            val formatter = NumberFormat.getNumberInstance(Locale.US)
            val formattedNanoTime = formatter.format(timeTaken)
            Log.d("MAMO", "Time taken to recognize faces: $formattedNanoTime ns")
            recognizedPersons
        }
}

