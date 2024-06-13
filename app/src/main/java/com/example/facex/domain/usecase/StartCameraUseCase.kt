package com.example.facex.domain.usecase

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.facex.domain.repository.CameraRepository
import javax.inject.Inject

class StartCameraUseCase @Inject constructor(
    private val cameraRepository: CameraRepository
) {
    operator fun invoke(preview: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraRepository.startCamera(preview, lifecycleOwner)
    }
}