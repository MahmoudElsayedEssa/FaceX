package com.example.particleeffect.particlize.system.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

sealed interface ParticleState {
    data class Active(
        val id: Long,
        val position: Offset,
        val velocity: Offset,  // Current velocity vector
        val scale: Float,
        val rotation: Float,
        val angularVelocity: Float,  // Rotation speed
        val alpha: Float,
        val color: Color,
        val progress: Float,
        val mass: Float = 1f,  // For physics interactions
        val lifeProgress: Float = 0f,  // Individual particle life
        val distanceFromOrigin: Float = 0f, // For radius-based effects
        val trail: List<Offset> = emptyList(), // For particle trails
        val radius: Float,
        val targetPosition: Offset,
    ) : ParticleState

    data object Dead : ParticleState
}
