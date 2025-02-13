package com.example.facex.ui.components.linechart.models

import androidx.compose.ui.graphics.Color

sealed class ChartTheme(
    val background: Color,
    val lineColor: Color,
    val gridColor: Color,
    val labelColor: Color,
    val selectionColor: Color
) {
    data object Light : ChartTheme(
        background = Color(0xFFF8F9FA),
        lineColor = Color(0xFF2196F3),
        gridColor = Color(0x4F000000),
        labelColor = Color(0xFF616161),
        selectionColor = Color(0xFF1976D2)
    )

    data object Dark : ChartTheme(
        background = Color(0xFF1E1E1E),
        lineColor = Color(0xFF64B5F6),
        gridColor = Color(0x4FFFFFFF),
        labelColor = Color(0xFFBDBDBD),
        selectionColor = Color(0xFF90CAF9)
    )
}
