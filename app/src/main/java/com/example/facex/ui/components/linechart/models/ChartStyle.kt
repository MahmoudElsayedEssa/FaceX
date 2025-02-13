package com.example.facex.ui.components.linechart.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartStyle(
    val lineColor: Color,
    val gridColor: Color,
    val labelColor: Color,
    val selectionColor: Color,
    val backgroundColor: Color,
    val lineWidth: Dp = 2.dp,
    val gridLineWidth: Dp = 1.dp,
    val labelTextSize: TextUnit = 12.sp,
    val gradientColors: List<Color>
)

fun ChartTheme.toStyle() = ChartStyle(
    lineColor = lineColor,
    gridColor = gridColor,
    labelColor = labelColor,
    selectionColor = selectionColor,
    backgroundColor = background,
    gradientColors = listOf(
        lineColor.copy(alpha = 0.2f), lineColor.copy(alpha = 0.0f)
    )
)
