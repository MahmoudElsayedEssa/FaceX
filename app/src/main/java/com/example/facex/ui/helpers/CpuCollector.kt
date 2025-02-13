package com.example.facex.ui.helpers

import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.SortedMap
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private object ProcConstants {
    const val PROC_PATH = "/proc"
    const val STAT_FILE = "stat"
    private const val CLOCK_TICKS = 100L // System clock ticks per second
    const val NANOS_PER_TICK = 1_000_000_000L / CLOCK_TICKS // Nanoseconds per clock tick

    enum class StatColumn(val index: Int) {
        USER_TIME(14), SYSTEM_TIME(15), NUM_THREADS(20), START_TIME(22)
    }
}


data class PreciseCpuMetrics(
    val usage: Double,
    val timestamp: Instant,
    val duration: Duration,
    val processCpuTime: Duration,
    val systemCpuTime: Duration,
    val processStats: ProcessStats
)


data class ProcessStats(
    val userTime: Long,
    val systemTime: Long,
    val numThreads: Int,
    val startTime: Long,
    val totalTime: Long = userTime + systemTime
)


sealed class CpuCollectorError : Exception() {
    data class ProcessStatReadError(override val cause: Throwable?) : CpuCollectorError()
    data class InvalidTimeError(override val message: String) : CpuCollectorError()
    data class CollectionError(override val cause: Throwable?) : CpuCollectorError()
}


object CpuCollector {
    private const val DEFAULT_SMOOTHING_FACTOR = 0.6
    private const val HISTORY_WINDOW_MILLIS = 5 * 60 * 1000L // 5 minutes
    private const val HISTORY_MAX_POINTS = 300 // Maximum points to store

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val lastProcessTime = AtomicLong(0L)
    private val lastSystemNanoTime = AtomicLong(System.nanoTime())
    private val lastUsage = AtomicReference(0.0)
    private val isCollecting = AtomicBoolean(false)
    private var smoothingFactor = DEFAULT_SMOOTHING_FACTOR

    // Change to SharedFlow with replay
    private val _metricsFlow = MutableSharedFlow<PreciseCpuMetrics>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val metricsFlow = _metricsFlow.asSharedFlow()

    // CPU History management
    private val _historyFlow = MutableStateFlow<CpuHistory>(CpuHistory())
    val historyFlow = _historyFlow.asStateFlow()

    private val _errorFlow = MutableSharedFlow<CpuCollectorError>()
    val errorFlow = _errorFlow.asSharedFlow()

    private var collectionJob: Job? = null

    data class CpuHistory(
        val startTime: Instant = Instant.now(),
        val data: SortedMap<Long, Double> = sortedMapOf()
    ) {
        fun addMeasurement(timestamp: Instant, value: Double): CpuHistory {
            val relativeTime = timestamp.toEpochMilli() - startTime.toEpochMilli()
            val newData = TreeMap(data)
            newData[relativeTime] = value

            // Remove old data points
            val cutoffTime = relativeTime - HISTORY_WINDOW_MILLIS
            newData.headMap(cutoffTime).clear()

            // If we have too many points, downsample
            if (newData.size > HISTORY_MAX_POINTS) {
                downsample(newData)
            }

            return copy(data = newData)
        }

        private fun downsample(data: TreeMap<Long, Double>) {
            val timestamps = data.keys.toList()
            val skipFactor = timestamps.size / HISTORY_MAX_POINTS

            if (skipFactor <= 1) return

            val toRemove = timestamps.filterIndexed { index, _ ->
                index % skipFactor != 0 && index != timestamps.lastIndex
            }
            toRemove.forEach { data.remove(it) }
        }
    }

    fun startCollecting(
        interval: Duration = 1.seconds,
        onError: (Throwable) -> Unit = {}
    ) {
        if (isCollecting.getAndSet(true)) return

        collectionJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val startTime = System.nanoTime()
                    val metrics = collectMetrics()

                    // Emit current metrics
                    _metricsFlow.emit(metrics)

                    // Update history
                    _historyFlow.update { history ->
                        history.addMeasurement(metrics.timestamp, metrics.usage)
                    }

                    val elapsed = System.nanoTime() - startTime
                    val remainingTime = (interval.inWholeNanoseconds - elapsed)
                        .coerceAtLeast(0)
                        .nanoseconds
                    delay(remainingTime)
                } catch (e: Exception) {
                    _errorFlow.emit(CpuCollectorError.CollectionError(e))
                    onError(e)
                    delay(1.seconds)
                }
            }
        }
    }

    fun stopCollecting() {
        if (!isCollecting.getAndSet(false)) return
        collectionJob?.cancel()
        collectionJob = null
    }

    fun shutdown() {
        stopCollecting()
        scope.cancel()
    }

    private suspend fun collectMetrics(): PreciseCpuMetrics {
        val startTime = System.nanoTime()
        val (totalCpuTime, numCores) = getTotalCpuTime()
        val endTime = System.nanoTime()

        val currentSystemNanos = (startTime + endTime) / 2
        val timeDiffNanos = currentSystemNanos - lastSystemNanoTime.getAndSet(currentSystemNanos)
        val processTimeDiff = totalCpuTime - lastProcessTime.getAndSet(totalCpuTime)

        if (timeDiffNanos <= 0) {
            throw CpuCollectorError.InvalidTimeError("Invalid time difference: $timeDiffNanos")
        }

        val processTimeNanos = processTimeDiff * ProcConstants.NANOS_PER_TICK
        val totalAvailableTime = timeDiffNanos * numCores

        val rawUsage = (processTimeNanos.toDouble() / totalAvailableTime.toDouble() * 100.0)
            .coerceIn(0.0, 100.0)

        val smoothedUsage = smoothUsage(rawUsage)
        lastUsage.set(smoothedUsage)

        return PreciseCpuMetrics(
            usage = smoothedUsage,
            timestamp = Instant.now(),
            duration = timeDiffNanos.nanoseconds,
            processCpuTime = processTimeNanos.nanoseconds,
            systemCpuTime = timeDiffNanos.nanoseconds,
            processStats = readProcessStats()
        )
    }

    private fun smoothUsage(rawUsage: Double): Double {
        val previousUsage = lastUsage.get()
        return (rawUsage * smoothingFactor + previousUsage * (1 - smoothingFactor))
            .coerceIn(0.0, 100.0)
            .roundToTwoDecimals()
    }

    private suspend fun readProcessStats(): ProcessStats = withContext(Dispatchers.IO) {
        try {
            val pid = Process.myPid()
            val statFile = File("${ProcConstants.PROC_PATH}/$pid/${ProcConstants.STAT_FILE}")

            val stats = statFile.readLines().firstOrNull()?.split(" ")
                ?: throw IllegalStateException("Unable to read process stats")

            fun getStat(column: ProcConstants.StatColumn): Long {
                return stats.getOrNull(column.index - 1)?.toLongOrNull()
                    ?: throw IllegalStateException("Invalid stat value for ${column.name}")
            }

            ProcessStats(
                userTime = getStat(ProcConstants.StatColumn.USER_TIME),
                systemTime = getStat(ProcConstants.StatColumn.SYSTEM_TIME),
                numThreads = getStat(ProcConstants.StatColumn.NUM_THREADS).toInt(),
                startTime = getStat(ProcConstants.StatColumn.START_TIME)
            )
        } catch (e: Exception) {
            throw CpuCollectorError.ProcessStatReadError(e)
        }
    }

    private fun getTotalCpuTime(): Pair<Long, Int> {
        return try {
            val pid = Process.myPid()
            val statFile = File("${ProcConstants.PROC_PATH}/$pid/${ProcConstants.STAT_FILE}")
            val stats = statFile.readLines().firstOrNull()?.split(" ")
                ?: throw IllegalStateException("Unable to read process stats")

            val numCores = Runtime.getRuntime().availableProcessors()
            val userTime = stats.getOrNull(ProcConstants.StatColumn.USER_TIME.index - 1)?.toLongOrNull() ?: 0L
            val systemTime = stats.getOrNull(ProcConstants.StatColumn.SYSTEM_TIME.index - 1)?.toLongOrNull() ?: 0L

            Pair(userTime + systemTime, numCores)
        } catch (e: Exception) {
            throw CpuCollectorError.ProcessStatReadError(e)
        }
    }
}
private fun Double.roundToTwoDecimals() = (this * 100).roundToInt() / 100.0
