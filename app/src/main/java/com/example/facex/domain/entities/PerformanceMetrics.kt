package com.example.facex.domain.entities

import kotlin.time.Duration


data class PerformanceMetrics(
    private val metrics: Map<PerformanceTracker.MetricKey, MetricData> = emptyMap()
) {
    data class MetricData(
        val lastValue: Duration,
        val average: Duration,
        val count: Int
    )

    fun updateMetric(key: PerformanceTracker.MetricKey, duration: Duration): PerformanceMetrics {
        val currentData = metrics[key]?.update(duration) ?: MetricData(duration, duration, 1)
        return copy(metrics = metrics + (key to currentData))
    }

    private fun MetricData.update(duration: Duration): MetricData {
        val newCount = count + 1
        val newAverage = (average * count + duration) / newCount
        return copy(lastValue = duration, average = newAverage, count = newCount)
    }

    fun reset(): PerformanceMetrics = copy(metrics = emptyMap())
    fun getAllMetrics(): Map<PerformanceTracker.MetricKey, MetricData> = metrics
}