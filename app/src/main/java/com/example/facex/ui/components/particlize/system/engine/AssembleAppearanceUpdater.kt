package com.example.particleeffect.particlize.system.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleState

class AssembleAppearanceUpdater(private val config: ParticleConfig.AppearanceConfig) {
    
    fun updateAppearance(
        particle: ParticleState.Active,
        progress: Float
    ): ParticleAppearance {
        // Start with small scale and grow as particles get closer to target
        val progressForScale = config.scaleEasing.transform(progress)
        val scale = interpolateValue(
            start = config.initialScale * 0.5f,  // Start smaller
            end = config.initialScale,           // End at target scale
            progress = progressForScale
        )

        // Fade in as particles assemble
        val progressForAlpha = config.alphaEasing.transform(progress)
        val alpha = interpolateValue(
            start = config.initialAlpha * 0.6f,  // Start more transparent
            end = config.initialAlpha,           // End at target alpha
            progress = progressForAlpha
        )

        // Color transition if configured
        val color = config.colorTransition?.let { transition ->
            interpolateColor(
                start = transition.startColor,
                end = transition.endColor,
                progress = transition.easing.transform(progress)
            )
        } ?: particle.color

        // Optional rotation during assembly
        val rotation = particle.rotation + particle.angularVelocity

        // Generate trail if configured
        val trail = if (config.trailConfig != null) {
            generateTrail(particle, config.trailConfig, alpha)
        } else {
            emptyList()
        }

        return ParticleAppearance(
            scale = scale,
            rotation = rotation,
            alpha = alpha,
            color = color,
            trail = trail
        )
    }

    private fun generateTrail(
        particle: ParticleState.Active,
        trailConfig: ParticleConfig.TrailConfig,
        baseAlpha: Float
    ): List<TrailOffset> {
        val trail = ArrayList<TrailOffset>(trailConfig.length)
        trail.add(TrailOffset(particle.position, baseAlpha))

        // Only add trail points when particle is moving
        if (particle.velocity != Offset.Zero) {
            val fadeStep = if (trailConfig.fadeOut) {
                (1f - trailConfig.minAlpha) / trailConfig.length
            } else 0f

            particle.trail.take(trailConfig.length - 1).forEachIndexed { index, position ->
                val trailAlpha = if (trailConfig.fadeOut) {
                    (baseAlpha * (1f - ((index + 1) * fadeStep))).coerceAtLeast(
                        trailConfig.minAlpha * baseAlpha
                    )
                } else baseAlpha
                
                trail.add(TrailOffset(position, trailAlpha))
            }
        }

        return trail
    }

    private fun interpolateValue(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }

    private fun interpolateColor(start: Color, end: Color, progress: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * progress,
            green = start.green + (end.green - start.green) * progress,
            blue = start.blue + (end.blue - start.blue) * progress,
            alpha = start.alpha + (end.alpha - start.alpha) * progress
        )
    }
}