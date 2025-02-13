package com.example.facex.ui.helpers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.SortedMap
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean

object FpsCollector {
    private const val HISTORY_WINDOW_MILLIS = 5 * 60 * 1000L // 5 minutes
    private const val HISTORY_MAX_POINTS = 300

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val isCollecting = AtomicBoolean(false)

    private val _metricsFlow = MutableSharedFlow<FpsMetric>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val metricsFlow = _metricsFlow.asSharedFlow()

    private val _historyFlow = MutableStateFlow(FpsHistory())
    val historyFlow = _historyFlow.asStateFlow()

    private var collectionJob: Job? = null

    data class FpsMetric(
        val fps: Int,
        val timestamp: Instant = Instant.now()
    )

    data class FpsHistory(
        val startTime: Instant = Instant.now(),
        val data: SortedMap<Long, Int> = sortedMapOf()
    ) {
        fun addMeasurement(timestamp: Instant, fps: Int): FpsHistory {
            val relativeTime = timestamp.toEpochMilli() - startTime.toEpochMilli()
            val newData = TreeMap(data)
            newData[relativeTime] = fps

            // Remove old data points
            val cutoffTime = relativeTime - HISTORY_WINDOW_MILLIS
            newData.headMap(cutoffTime).clear()

            // Downsample if too many points
            if (newData.size > HISTORY_MAX_POINTS) {
                downsample(newData)
            }

            return copy(data = newData)
        }

        private fun downsample(data: TreeMap<Long, Int>) {
            val timestamps = data.keys.toList()
            val skipFactor = timestamps.size / HISTORY_MAX_POINTS

            if (skipFactor <= 1) return

            val toRemove = timestamps.filterIndexed { index, _ ->
                index % skipFactor != 0 && index != timestamps.lastIndex
            }
            toRemove.forEach { data.remove(it) }
        }
    }

    fun updateFps(fps: Int) {
        scope.launch {
            val metric = FpsMetric(fps)
            _metricsFlow.emit(metric)
            _historyFlow.update { history ->
                history.addMeasurement(metric.timestamp, fps)
            }
        }
    }

    fun clear() {
        scope.launch {
            _historyFlow.update { FpsHistory() }
        }
    }
}
