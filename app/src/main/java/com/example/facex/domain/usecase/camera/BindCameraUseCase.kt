package com.example.facex.domain.usecase.camera

import androidx.lifecycle.LifecycleOwner
import com.example.facex.domain.entities.CameraState
import com.example.facex.domain.repository.CameraRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BindCameraUseCase @Inject constructor(
    private val cameraRepository: CameraRepository
) {
    suspend operator fun invoke(lifecycleOwner: LifecycleOwner): Flow<CameraState> {
        return cameraRepository.startCamera(lifecycleOwner)
    }
}