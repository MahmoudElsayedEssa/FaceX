package com.example.facex.ui.screens.camera_face_recognition.components.fabs

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec

object AnimationConstants {
    // Icon animations
    const val ICON_START_DELAY = 150
    const val ICON_BASE_DELAY = 300
    const val ICON_ANIMATION_DURATION = 300
    const val ICON_SCALE_DURATION = 300
    const val ICON_JUMP_HEIGHT = 20f

    // Coordinated delays
    const val POSITION_SETTLE_DELAY = 300

    // Scale values
    const val ICON_INITIAL_SCALE = 0.2f
    const val ICON_MID_SCALE = 0.7f
    const val ICON_PEAK_SCALE = 1.2f
    const val ICON_FINAL_SCALE = 1f

    // Spring configurations


    val ICON_SPRING_SPEC = SpringSpec<Float>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessLow
    )
    // Duration constants
    const val EXPAND_ANIMATION_DURATION = 300
    const val COLLAPSE_ANIMATION_DURATION = 250
    const val SCALE_ANIMATION_DURATION = 200
    const val ALPHA_ANIMATION_DURATION = 150

    // Delays
    const val CHILD_ANIMATION_DELAY = 50
    const val CHILD_STAGGER_DELAY = 50

    // Spring configurations
    val EXPAND_SPRING_SPEC = SpringSpec<Float>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessLow
    )

    val COLLAPSE_SPRING_SPEC = SpringSpec<Float>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMedium
    )

    // Scale configurations
    const val EXPANDED_SCALE = 1f
    const val COLLAPSED_SCALE = 0.8f
    const val OVERSHOOT_FACTOR = 1.2f
}