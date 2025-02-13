package com.example.particleeffect.particlize.system.core

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class ParticleConfig(
    val particleDensity: Int,
    val animation: AnimationProperties,
    val emission: EmissionConfig,
    val appearance: AppearanceConfig,
    val motion: MotionConfig
) {
    data class AnimationProperties(
        val duration: Duration,  // Duration in milliseconds
        val easing: Easing = LinearEasing,
        val delay: Duration = Duration.ZERO
    )

    data class StaggerConfig(
        val mode: StaggerMode,
        val delayPerParticle: Duration = 50.toDuration(DurationUnit.MILLISECONDS)
    ) {
        enum class StaggerMode {
            TopToBottom,    // Start from top row
            BottomToTop,    // Start from bottom row
            Random,         // Random start times
            InsideOut,     // Start from center
            OutsideIn      // Start from edges
        }
    }

    sealed interface EmissionPattern {
        // How particles are distributed in the shape
        data class Line(
            val angle: Float = 0f,  // Direction of emission
            val spread: Float = 30f // Spread angle
        ) : EmissionPattern

        data class Radial(
            val radius: Float, val startAngle: Float = 0f, val endAngle: Float = 360f
        ) : EmissionPattern

        data class Burst(
            val count: Int,     // Number of particles per burst
            val radius: Float,  // Initial radius
            val angle: Float = 360f  // Spread angle
        ) : EmissionPattern

        // For our Thanos effect case
        data class Grid(
            val spacing: Int,  // Space between particles
            val jitter: Float = 0f  // Random offset
        ) : EmissionPattern
    }

    data class EmissionConfig(
        val pattern: EmissionPattern, val rate: Float = 1f,  // Particles per frame
        val lifetime: Duration,     // How long particles live
        val stagger: StaggerConfig
    )


    data class AppearanceConfig(
        val initialScale: Float = 1f,
        val finalScale: Float = 0f,
        val initialAlpha: Float = 1f,
        val finalAlpha: Float = 0f,
        val scaleEasing: Easing = LinearEasing,
        val alphaEasing: Easing = LinearEasing,
        val colorTransition: ColorTransition? = null,
        val blendMode: BlendMode = BlendMode.SrcOver,
        val trailConfig: TrailConfig? = null
    )

    data class ColorTransition(
        val startColor: Color, val endColor: Color, val easing: Easing = LinearEasing
    )

    data class MotionConfig(
        val initialVelocity: Float = 0f,
        val gravity: Float = 0f,
        val drag: Float = 0f,
        val rotationSpeed: Float = 0f,
        val randomizeDirection: Boolean = true,
        val bounce: Float = 0f,  // Bounce factor when hitting boundaries
        val turbulence: TurbulenceConfig = TurbulenceConfig(),
        val attractionPoint: AttractionConfig? = null,
        val collisionBehavior: CollisionBehavior = CollisionBehavior.None,
        val spread: Float = 400f,
        val angle: Float
    )

    data class TrailConfig(
        val length: Int = 5, val fadeOut: Boolean = true, val minAlpha: Float = 0.2f
    )

    data class TurbulenceConfig(
        val frequency: Float = 1f,
        val amplitude: Float = 1f,
        val octaves: Int = 1
    )

    data class AttractionConfig(
        val point: Offset, val strength: Float, val radius: Float
    )


    enum class CollisionBehavior {
        None, Bounce, Merge, Split
    }

    companion object {
        val default = ParticleConfig(
            particleDensity = 4,
            animation = AnimationProperties(
                duration = 20.milliseconds, easing = LinearEasing
            ), emission = EmissionConfig(
                pattern = EmissionPattern.Line(
                    angle = -90f,
                    spread = 3000f
                ), lifetime = 2000.milliseconds, stagger = StaggerConfig(
                    mode = StaggerConfig.StaggerMode.TopToBottom,
                    delayPerParticle = 50.milliseconds
                )
            ), appearance = AppearanceConfig(), motion = MotionConfig(
                initialVelocity = 80f,
                randomizeDirection = true,
                angle = -45f
            )
        )

    }
}
