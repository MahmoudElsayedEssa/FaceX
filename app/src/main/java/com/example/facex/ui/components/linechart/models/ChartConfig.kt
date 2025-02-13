package com.example.facex.ui.components.linechart.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ChartConfig(
    val theme: ChartTheme = ChartTheme.Light,
    val initialZoom: Float = 1f,
    val enableDoubleTapZoom: Boolean = true,
    val pointSelectionRadius: Dp = 20.dp,
    val autoScrollThreshold: Long = 250L,
    val dimensions: ChartDimensions = ChartDimensions(),
    val formatters: ChartFormatters = ChartFormatters()
)
