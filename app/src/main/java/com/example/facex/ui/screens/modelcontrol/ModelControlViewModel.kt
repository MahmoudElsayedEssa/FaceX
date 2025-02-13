package com.example.facex.ui.screens.modelcontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.entities.ModelAcceleration
import com.example.facex.domain.entities.ModelOption
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.repository.MLConfigurationRepository
import com.example.facex.domain.usecase.ChangeAccelerationUseCase
import com.example.facex.domain.usecase.ChangeDetectionServiceUseCase
import com.example.facex.domain.usecase.ChangeDetectionThresholdUseCase
import com.example.facex.domain.usecase.ChangeRecognitionServiceUseCase
import com.example.facex.domain.usecase.ChangeRecognitionThresholdUseCase
import com.example.facex.domain.usecase.ChangeServiceModelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ModelControlViewModel @Inject constructor(
    private val changeAccelerationUseCase: ChangeAccelerationUseCase,
    private val changeRecognitionThresholdUseCase: ChangeRecognitionThresholdUseCase,
    private val changeDetectionThresholdUseCase: ChangeDetectionThresholdUseCase,
    private val changeServiceModelUseCase: ChangeServiceModelUseCase,
    private val changeDetectionServiceUseCase: ChangeDetectionServiceUseCase,
    private val changeRecognitionServiceUseCase: ChangeRecognitionServiceUseCase,
    private val mlConfigurationRepository: MLConfigurationRepository,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow(
        ModelControlState(
            isLoading = false,
            detectionServices = mlConfigurationRepository.detectionServices.value,
            recognitionServices = mlConfigurationRepository.recognitionServices.value,
            selectedDetectionIndex = -1,
            selectedRecognitionIndex = -1,
            lastError = null
        )
    )
    val state: StateFlow<ModelControlState> = _state.asStateFlow()

    init {
        // Observe changes to service lists from the repository.
        viewModelScope.launch {
            mlConfigurationRepository.detectionServices.collect { newList ->
                updateState { copy(detectionServices = newList) }
            }
        }
        viewModelScope.launch {
            mlConfigurationRepository.recognitionServices.collect { newList ->
                updateState { copy(recognitionServices = newList) }
            }
        }
    }


    // Helper to update state in a unidirectional data flow.
    private fun updateState(modifier: ModelControlState.() -> ModelControlState) {
        _state.update(modifier)
    }

    /**
     * Change the threshold (e.g. detection confidence) for the given service.
     * For detection services, the active model is taken as the one marked with isCurrent.
     */
    fun changeDetectionThreshold(serviceOption: ServiceOption, newThreshold: Float) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, lastError = null) }
            delay(800)
            val currentModel = serviceOption.models?.firstOrNull { it.isCurrent }
                ?: run {
                    updateState { copy(isLoading = false, lastError = "No current model found") }
                    return@launch
                }
            changeDetectionThresholdUseCase(serviceOption, newThreshold)
                .onFailure { error ->
                    updateState { copy(lastError = error.message) }
                }
            val updatedServices = mlConfigurationRepository.detectionServices.value.map { service ->
                if (service.id == serviceOption.id) {
                    // Update the model options inside the service:
                    service.copy(models = service.models?.map { model ->
                        if (model.id == currentModel.id) {
                            model.copy(threshold = newThreshold)
                        } else {
                            model
                        }
                    })
                } else {
                    service
                }
            }
            mlConfigurationRepository.updateDetectionServices(updatedServices)

            updateState { copy(isLoading = false) }
        }
    }

    fun changeRecognitionThreshold(serviceOption: ServiceOption, newThreshold: Float) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, lastError = null) }
            delay(800)
            val currentModel = serviceOption.models?.firstOrNull { it.isCurrent }
                ?: run {
                    updateState { copy(isLoading = false, lastError = "No current model found") }
                    return@launch
                }
            changeRecognitionThresholdUseCase(serviceOption, newThreshold)
                .onFailure { error ->
                    updateState { copy(lastError = error.message) }
                }

            val updatedServices =
                mlConfigurationRepository.recognitionServices.value.map { service ->
                    if (service.id == serviceOption.id) {
                        // Update the model options inside the service:
                        service.copy(models = service.models?.map { model ->
                            if (model.id == currentModel.id) {
                                model.copy(threshold = newThreshold)
                            } else {
                                model
                            }
                        })
                    } else {
                        service
                    }
                }
            mlConfigurationRepository.updateRecognitionServices(updatedServices)
            updateState { copy(isLoading = false) }
        }
    }

    fun changeAcceleration(serviceOption: ServiceOption, newAcceleration: ModelAcceleration) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, lastError = null) }
            delay(800)
            changeAccelerationUseCase(serviceOption, newAcceleration)
                .onSuccess {
                    delay(800)
                    val updatedServices =
                        mlConfigurationRepository.recognitionServices.value.map { service ->
                            if (service.id == serviceOption.id) {
                                service.copy(models = service.models?.map { model ->
                                    if (model.isCurrent) model.copy(modelAcceleration = newAcceleration) else model
                                })
                            } else service
                        }

                    mlConfigurationRepository.updateRecognitionServices(updatedServices)
                }
                .onFailure { error ->
                    updateState { copy(lastError = error.message) }
                }

            updateState { copy(isLoading = false) }
        }
    }

    /**
     * Change the active model for a given service.
     */
    fun changeServiceModel(serviceOption: ServiceOption, newModel: ModelOption) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, lastError = null) }
            changeServiceModelUseCase(serviceOption, newModel)
                .onFailure { error ->
                    updateState { copy(lastError = error.message) }
                }
            updateState { copy(isLoading = false) }
        }
    }

    /**
     * Switch the detection service (e.g., from MediaPipe to MLKit).
     */
    fun switchDetectionService(serviceOption: ServiceOption) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, lastError = null) }
            changeDetectionServiceUseCase.execute(serviceOption)
                .onFailure { error ->
                    updateState { copy(lastError = error.message) }
                }
            updateState { copy(isLoading = false) }
        }
    }

    /**
     * Switch the recognition service (e.g., from LiteRT to OpenCV).
     */
    fun switchRecognitionService(serviceOption: ServiceOption) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, lastError = null) }
            changeRecognitionServiceUseCase.execute(serviceOption)
                .onFailure { error ->
                    updateState { copy(lastError = error.message) }
                }
            updateState { copy(isLoading = false) }
        }
    }

    /**
     * Optionally, support reordering the service list.
     */
    fun moveDetectionServiceToTop(selectedItem: ServiceOption) {
        updateState {
            copy(detectionServices = detectionServices.toMutableList().apply {
                val index = indexOfFirst { it.id == selectedItem.id }
                if (index > 0) {
                    val item = removeAt(index).copy(isCurrent = true)
                    if (isNotEmpty()) {
                        val previousTop = removeAt(0).copy(isCurrent = false)
                        add(0, previousTop)
                    }
                    add(0, item)
                }
            })
        }
    }

    fun moveRecognitionServiceToTop(selectedItem: ServiceOption) {
        updateState {
            copy(recognitionServices = recognitionServices.toMutableList().apply {
                val index = indexOfFirst { it.id == selectedItem.id }
                if (index > 0) {
                    val item = removeAt(index).copy(isCurrent = true)
                    if (isNotEmpty()) {
                        val previousTop = removeAt(0).copy(isCurrent = false)
                        add(0, previousTop)
                    }
                    add(0, item)
                }
            })
        }
    }
}