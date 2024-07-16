package com.example.facex.domain.usecase.camera

import com.example.facex.domain.repository.CameraRepository
import javax.inject.Inject

class SetLinearZoomUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    operator fun invoke(x: Float) {
        cameraRepository.setLinearZoom(x)
    }
}