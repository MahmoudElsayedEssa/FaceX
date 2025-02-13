package com.example.facex.domain.performancetracking

import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

data class MetricData(
    private val windowSize: Int,
    private val samples: ConcurrentLinkedDeque<Duration> = ConcurrentLinkedDeque()
) {
    var lastValue: Duration = ZERO
        private set

    val averageFps: Int
        get() = if (!average.isZero) {
            (1000 / average.inWholeMilliseconds).toInt().coerceIn(0, 160)
        } else 0

    val average: Duration
        get() = if (samples.isNotEmpty()) {
            samples.sumOf { it.inWholeMilliseconds }.milliseconds / samples.size
        } else ZERO

    @Synchronized
    fun update(duration: Duration) {
        if (duration.isNegative()) return

        lastValue = duration
        samples.addLast(duration)

        if (samples.size > windowSize) {
            samples.removeFirst()
        }
    }
}

val Duration.isZero: Boolean
    get() = this.inWholeNanoseconds == 0L
