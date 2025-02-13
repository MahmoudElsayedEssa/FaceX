package com.example.facex.ui.components.linechart.models

sealed class InteractionMode {
    data object Normal : InteractionMode()
    data object SkipNavigation : InteractionMode()
}
