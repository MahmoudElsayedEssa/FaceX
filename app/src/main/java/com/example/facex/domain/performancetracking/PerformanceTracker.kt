package com.example.facex.domain.performancetracking

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.TimeSource

interface PerformanceTracker {
    val metricsFlow: SharedFlow<PerformanceMetrics>
    suspend fun <T> suspendTrack(metric: String, block: suspend () -> T): T
    fun <T> track(metric: String, block: () -> T): T
    fun clear()
}