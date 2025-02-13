package com.example.facex.ui.components.linechart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.example.facex.ui.components.linechart.models.InteractionMode

class InteractionState(
    val enableDoubleTapZoom: Boolean,
    val pointSelectionRadius: Dp
) {
    var skipDirection by mutableStateOf(SkipDirection.NONE)
    var skipCount by mutableIntStateOf(0)
    var showIndicator by mutableStateOf(false)
    var lastTapInfo by mutableStateOf(0L to Offset.Zero)
    var interactionMode by mutableStateOf<InteractionMode>(InteractionMode.Normal)
}
