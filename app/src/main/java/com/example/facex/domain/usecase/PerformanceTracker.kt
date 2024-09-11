package com.example.facex.domain.usecase

import com.example.facex.domain.entities.PerformanceMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceTracker @Inject constructor() {
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()

    fun updateMetric(key: String, nanoTime: Long) {
        _performanceMetrics.update { currentMetrics ->
            val formattedTime = formatNanoTime(nanoTime)
            currentMetrics.copy(metrics = currentMetrics.metrics + (key to formattedTime))
        }
    }

    private fun formatNanoTime(nanoTime: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "${formatter.format(nanoTime)} ns"
    }

    fun clear() {
        _performanceMetrics.value = PerformanceMetrics()
    }

    companion object {
        const val RECOGNITION_TIME = "Face Recognition Time"
        const val DETECTION_TIME = "Face Detection Time"
        const val TOTAL_ANALYSIS_TIME = "Total Analysis Time"
        const val FRAME_ENQUEUE_TIME = "Frame Enqueue Time"
        const val FRAME_PROCESSING_TIME = "Frame Processing Time"
        const val FRAME_BITMAP_CONVERSION_TIME = "Image to Bitmap Conversion Time"

    }
}

