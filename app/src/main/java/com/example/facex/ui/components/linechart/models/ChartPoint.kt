package com.example.facex.ui.components.linechart.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

@Immutable
data class ChartPoint(
    val timestamp: Long,
    val value: Double,
    val metadata: Map<String, Any> = emptyMap()
)