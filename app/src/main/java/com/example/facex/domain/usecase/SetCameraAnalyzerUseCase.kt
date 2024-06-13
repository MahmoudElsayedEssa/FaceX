package com.example.facex.domain.usecase

import com.example.facex.data.local.camera.ImageAnalyzer
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.repository.CameraRepository
import javax.inject.Inject

class SetCameraAnalyzerUseCase @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val recognizeFaces: RecognizeFacesUseCase,
) {

    operator fun invoke(
        persons: List<Person>,
        onDetectFace: (List<DetectedFace>) -> Unit,
        onRecognizedPerson: (List<DetectedFace>, List<RecognizedPerson>?) -> Unit
    ) {
        val analyzer = ImageAnalyzer { bitmap, rotationDegrees ->
            recognizeFaces(
                bitmap = bitmap,
                rotationDegrees= rotationDegrees,
                persons = persons,
                onDetectFace = onDetectFace,
                onRecognizedPerson = onRecognizedPerson
            )
        }
        cameraRepository.setImageAnalyzer(analyzer)
    }

    companion object {
        private const val TAG = "SetCameraAnalyzerUse"
    }

}
