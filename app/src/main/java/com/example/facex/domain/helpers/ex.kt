package com.example.facex.domain.helpers

import com.example.facex.domain.usecase.PerformanceTracker

inline fun <T> measureExecutionTime(block: () -> T): Pair<T, Long> {
    val startTime = System.nanoTime()
    val result = block()
    val endTime = System.nanoTime()
    return result to (endTime - startTime)
}


inline fun <T> measureAndTrackPerformance(
    performanceTracker: PerformanceTracker,
    metric: String,
    block: () -> T
): T {
    val (result, time) = measureExecutionTime(block)
    performanceTracker.updateMetric(metric, time)
    return result
}