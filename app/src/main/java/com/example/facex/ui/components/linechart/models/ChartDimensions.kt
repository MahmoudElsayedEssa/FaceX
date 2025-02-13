package com.example.facex.ui.components.linechart.models

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ChartDimensions(
    val xSteps: Int = 6,
    val ySteps: Int = 5,
    val contentPadding: PaddingValues = PaddingValues(16.dp),
    val pointRadius: Dp = 4.dp,
    val selectedPointRadius: Dp = 8.dp,
    val gridLineWidth: Dp = 1.dp,
    val lineWidth: Dp = 2.dp
)
