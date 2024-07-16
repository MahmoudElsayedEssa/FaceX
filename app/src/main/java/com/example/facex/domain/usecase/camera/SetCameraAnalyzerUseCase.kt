package com.example.facex.domain.usecase.camera

import android.content.Context
import com.example.facex.data.local.camera.FacesImageAnalyzer
import com.example.facex.di.DefaultDispatcher
import com.example.facex.di.MainDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import com.example.facex.domain.logExecutionTime
import com.example.facex.domain.repository.CameraRepository
import com.example.facex.domain.usecase.RecognizeFacesUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetCameraAnalyzerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val recognizeFaces: RecognizeFacesUseCase,
    private val cameraRepository: CameraRepository,
) {
    suspend operator fun invoke(
        onRecognizeFaces: (Pair<List<DetectedFace>, List<RecognizedPerson>>) -> Unit
    ) {
        val facesAnalyzer = FacesImageAnalyzer(context) { bitmap, rotationDegrees ->
            logExecutionTime("FaceRecognition", "Face recognition") {
                runBlocking(defaultDispatcher) {
                    val result = recognizeFaces(bitmap, rotationDegrees)
                    withContext(mainDispatcher) {
                        onRecognizeFaces(result) // Adjust as needed
                    }
                }
            }
        }


        withContext(mainDispatcher) {
            cameraRepository.setImageAnalyzer(facesAnalyzer)
        }
    }
}
