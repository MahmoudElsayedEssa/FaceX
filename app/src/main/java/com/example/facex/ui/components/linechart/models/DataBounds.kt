package com.example.facex.ui.components.linechart.models

import androidx.compose.runtime.Immutable

@Immutable
data class DataBounds(
    val points: List<ChartPoint>,
    val minTime: Long = points.firstOrNull()?.timestamp ?: 0L,
    val maxTime: Long = points.lastOrNull()?.timestamp ?: 0L,
    val minValue: Double = 0.0,
    val maxValue: Double = points.maxOfOrNull { it.value }
        ?.coerceAtLeast(ChartDefaults.DEFAULT_MAX_VALUE) ?: ChartDefaults.DEFAULT_MAX_VALUE
) {
    init {
        require(minTime <= maxTime) { "minTime must be less than or equal to maxTime" }
        require(minValue <= maxValue) { "minValue must be less than or equal to maxValue" }
    }

}
