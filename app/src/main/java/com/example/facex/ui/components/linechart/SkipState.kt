package com.example.facex.ui.components.linechart

data class SkipState(
    val direction: SkipDirection = SkipDirection.NONE,
    val consecutiveTaps: Int = 0,
    val lastTapTime: Long = 0L,
    val isActive: Boolean = false
)

enum class SkipDirection {
    FORWARD, BACKWARD, NONE
}
