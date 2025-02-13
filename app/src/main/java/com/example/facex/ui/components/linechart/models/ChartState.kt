package com.example.facex.ui.components.linechart.models

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size

@Stable
data class ChartState(
    var viewport: ChartViewport,
    val dataBounds: DataBounds,
    val size: Size = Size.Zero,
    val pinnedPoint: ChartPoint? = null,
    val isAutoScrolling: Boolean = false,
    val zoomLevel: Float = 1f
)