package com.example.facex.domain.usecase

import com.example.facex.data.local.camera.ImageAnalyzer
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.repository.CameraRepository
import com.example.facex.domain.repository.PersonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetCameraAnalyzerUseCase @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val recognizeFaces: RecognizeFacesUseCase,
    private val personRepository: PersonRepository

) {
    suspend operator fun invoke(
        onRecognizeFaces: (Flow<Pair<List<DetectedFace>, List<RecognizedPerson>?>>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            personRepository.getAllPersons().collect {
                val analyzer = ImageAnalyzer { bitmap, rotationDegrees ->
                    val result = recognizeFaces(
                        bitmap = bitmap,
                        rotationDegrees = rotationDegrees,
                        persons = it
                    )
                    onRecognizeFaces(result)
                }
                cameraRepository.setImageAnalyzer(analyzer)
            }
        }
    }

    companion object {
        private const val TAG = "SetCameraAnalyzerUse"
    }

}
