package com.example.facex.ui.components.linechart.models

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object ChartDefaults {
    const val MIN_ZOOM = 0.5f
    const val MAX_ZOOM = 5f
    const val MIN_VIEWPORT_DURATION = 1_000L // 1 second
    const val DEFAULT_VIEWPORT_DURATION = 5_000L // 5 seconds
    const val DEFAULT_MAX_VALUE = 100.0
    const val DOUBLE_TAP_TIMEOUT = 300L
    const val BASE_SKIP_AMOUNT = 1000L
    const val SKIP_MODE_TIMEOUT = 1_500L
    const val DEFAULT_POINT_SELECTION_THRESHOLD = 40f // in pixels
    const val DOUBLE_TAP_DISTANCE = 50f// in pixels


    val DEFAULT_CONTENT_PADDING = PaddingValues(
        start = 32.dp,  // Space for y-axis labels
        end = 16.dp,    // Right padding
        top = 16.dp,    // Top padding
        bottom = 16.dp  // Space for x-axis labels
    )
}
