package com.example.facex.domain.usecase

import com.example.facex.domain.entities.PerformanceMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceTracker @Inject constructor() {
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()

    fun updateMetric(key: String, nanoTime: Long) {
        _performanceMetrics.update { currentMetrics ->
            currentMetrics.copy(metrics = currentMetrics.metrics + (key to nanoTime))
        }
    }

    fun getMetric(key: String): Long? {
        return _performanceMetrics.value.metrics[key]
    }

    private fun formatTime(nanoTime: Long): String {
        val milliseconds = nanoTime / 1_000_000
        val seconds = milliseconds / 1_000
        val minutes = seconds / 60

        return when {
            minutes > 0 -> String.format(Locale.US, "%d min %d sec", minutes, seconds % 60)
            seconds > 0 -> String.format(Locale.US, "%d sec %d ms", seconds, milliseconds % 1_000)
            milliseconds > 0 -> String.format(
                Locale.US,
                "%d ms %d µs",
                milliseconds,
                (nanoTime % 1_000_000) / 1_000
            )

            else -> String.format(Locale.US, "%d ns", nanoTime)
        }
    }

    fun clear() {
        _performanceMetrics.value = PerformanceMetrics()
    }

    companion object {
        const val TAG = "PerformanceTracker"
        const val RECOGNITION_TIME = "Face Recognition Time"
        const val DETECTION_TIME = "Face Detection Time"
        const val EMBEDDING_TIME = "Embedding Time"
        const val FIND_RECOGNIZED_PERSON_TIME = "Find Recognized Person Time"
        const val FRAME_CROPPING_TIME = "Image Cropping Time"
        const val CONVERT_TO_GRAY_SCALE_TIME = "Convert to Gray Scale Time"
        const val TOTAL_ANALYSIS_TIME = "Total Analysis Time"
        const val FRAME_ENQUEUE_TIME = "Frame Enqueue Time"
        const val FRAME_PROCESSING_TIME = "Frame Processing Time"
        const val FRAME_BITMAP_CONVERSION_TIME = "Image to Bitmap Conversion Time"
    }
}

