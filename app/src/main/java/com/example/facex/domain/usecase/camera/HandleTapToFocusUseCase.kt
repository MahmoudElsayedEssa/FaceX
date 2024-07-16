package com.example.facex.domain.usecase.camera

import com.example.facex.domain.repository.CameraRepository
import javax.inject.Inject

class HandleTapToFocusUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    operator fun invoke(x: Float, y: Float) {
        cameraRepository.handleTapToFocus(x, y)
    }
}