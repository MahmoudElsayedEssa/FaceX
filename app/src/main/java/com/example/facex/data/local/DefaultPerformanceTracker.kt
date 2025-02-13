package com.example.facex.data.local

import com.example.facex.domain.performancetracking.PerformanceMetrics
import com.example.facex.domain.performancetracking.PerformanceTracker
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.TimeSource

object DefaultPerformanceTracker : PerformanceTracker {
    private val _metricsFlow = MutableSharedFlow<PerformanceMetrics>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val metricsFlow = _metricsFlow.asSharedFlow()

    // Keep track of current metrics for updates
    private val currentMetrics = AtomicReference(PerformanceMetrics())

    private fun trackMetric(metricName: String, duration: Duration) {
        currentMetrics.getAndUpdate { metrics ->
            metrics.updateMetric(metricName, duration)
        }.also { updatedMetrics ->
            _metricsFlow.tryEmit(updatedMetrics)
        }
    }

    override fun clear() {
        currentMetrics.set(PerformanceMetrics())
        _metricsFlow.tryEmit(PerformanceMetrics())
    }

    override suspend fun <T> suspendTrack(metric: String, block: suspend () -> T): T {
        val mark = TimeSource.Monotonic.markNow()
        return try {
            block()
        } finally {
            val duration = mark.elapsedNow()
            trackMetric(metric, duration)
        }
    }

    override fun <T> track(metric: String, block: () -> T): T {
        val mark = TimeSource.Monotonic.markNow()
        return try {
            block()
        } finally {
            val duration = mark.elapsedNow()
            trackMetric(metric, duration)
        }
    }
}