package com.example.facex.ui.components.linechart.models

import androidx.compose.runtime.Immutable
import com.example.facex.ui.utils.formatTimeValue

@Immutable
data class ChartFormatters(
    val timeFormatter: (Long) -> String = { timestamp ->
        timestamp.formatTimeValue()
    }, val valueFormatter: (Double) -> String = { value ->
        "%.1f".format(value)
    }
)
