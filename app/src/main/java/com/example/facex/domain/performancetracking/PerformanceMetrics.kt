package com.example.facex.domain.performancetracking

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

data class PerformanceMetrics(
    private val metrics: ConcurrentHashMap<String, MetricData> = ConcurrentHashMap(),
    private val capacity: Int = DEFAULT_CAPACITY
) {
    fun updateMetric(key: String, duration: Duration): PerformanceMetrics {
        metrics.getOrPut(key) { MetricData(capacity) }.update(duration)
        return this
    }

    fun reset(): PerformanceMetrics {
        metrics.clear()
        return this
    }

    fun metricDataMap():
            Map<String, MetricData> = metrics.toMap()

    companion object {
        private const val DEFAULT_CAPACITY = 100
    }
}
