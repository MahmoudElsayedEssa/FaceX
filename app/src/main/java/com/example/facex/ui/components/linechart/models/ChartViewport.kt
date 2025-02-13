package com.example.facex.ui.components.linechart.models

import androidx.compose.runtime.Immutable


@Immutable
data class ChartViewport(
    val startTime: Long,
    val duration: Long,
    val minValue: Double,
    val maxValue: Double,
    val zoomLevel: Float = 1f
) {

    companion object {
        fun createDefault(points: List<ChartPoint>): ChartViewport = when {
            points.isEmpty() -> {
                val now = System.currentTimeMillis()
                ChartViewport(
                    startTime = now,
                    duration = ChartDefaults.DEFAULT_VIEWPORT_DURATION,
                    minValue = 0.0,
                    maxValue = ChartDefaults.DEFAULT_MAX_VALUE
                )
            }

            else -> {
                val firstPoint = points.first()
                val maxValue =
                    points.maxOf { it.value }.coerceAtLeast(ChartDefaults.DEFAULT_MAX_VALUE)
                ChartViewport(
                    startTime = firstPoint.timestamp,
                    duration = ChartDefaults.DEFAULT_VIEWPORT_DURATION,
                    minValue = 0.0,
                    maxValue = maxValue
                )
            }
        }
    }
}
