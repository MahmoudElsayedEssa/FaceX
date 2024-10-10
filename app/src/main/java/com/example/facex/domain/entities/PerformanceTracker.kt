package com.example.facex.domain.entities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.measureTimedValue


@Singleton
class PerformanceTracker {

    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()

    fun updateMetric(key: MetricKey, duration: Duration) {
        _performanceMetrics.update { it.updateMetric(key, duration) }
    }

    fun clear() {
        _performanceMetrics.update { it.reset() }
    }

    inline fun <T> measurePerformance(metric: MetricKey, crossinline block: () -> T): T {
        val (result, duration) = measureTimedValue { block() }
        updateMetric(metric, duration)
        return result
    }

    suspend inline fun <T> measureSuspendPerformance(
        metric: MetricKey,
        crossinline block: suspend () -> T
    ): T {
        val (result, duration) = measureTimedValue { block() }
        updateMetric(metric, duration)
        return result
    }

    fun printAllMetrics() {
        performanceMetrics.value.getAllMetrics().forEach { (key, data) ->
            println("${key}: last=${data.lastValue}, avg=${data.average}, count=${data.count}")
        }
    }


    enum class MetricKey {
        RECOGNITION_TIME,
        DETECTION_TIME,
        EMBEDDING_TIME,
        FIND_RECOGNIZED_PERSON_TIME,
        FRAME_CROPPING_TIME,
        TOTAL_PROCESSING_TIME,
        CONVERSION_TIME,
        GRAYSCALE_TIME,
        SCALING_TIME,
        GET_PERSONS,
        CREATE_RECOGNIZER,
        FRAME_PROCESSING_TIME;

        override fun toString(): String = name.replace("_", " ")
            .lowercase(Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
}


