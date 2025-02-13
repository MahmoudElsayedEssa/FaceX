package com.example.particleeffect.particlize.system.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleState

data class TrailOffset(
    val position: Offset,
    val alpha: Float
)

data class ParticleAppearance(
    val scale: Float,
    val rotation: Float,
    val alpha: Float,
    val color: Color,
    val trail: List<TrailOffset>
)



class DisintegrationAppearanceUpdater(private val config: ParticleConfig.AppearanceConfig) {
    // Cache for commonly used values
    private val colorTransitionEnabled = config.colorTransition != null
    private val trailEnabled = config.trailConfig != null
    private val initialTrailCapacity = config.trailConfig?.length ?: 0

    // Reusable collections to reduce allocations
    private val trailList = ArrayList<TrailOffset>(initialTrailCapacity)

    fun updateAppearance(
        particle: ParticleState.Active,
        lifeProgress: Float
    ): ParticleAppearance {
        val clampedProgress = lifeProgress.coerceIn(0f, 1f)

        // Calculate scale and alpha in one pass if using same easing
        val (scale, alpha) = if (config.scaleEasing === config.alphaEasing) {
            val easedProgress = config.scaleEasing.transform(clampedProgress)
            Pair(
                interpolateValue(
                    config.initialScale,
                    config.finalScale,
                    easedProgress
                ).coerceAtLeast(0f),
                interpolateValue(config.initialAlpha, config.finalAlpha, easedProgress).coerceIn(
                    0f,
                    1f
                ) * 100
            )
        } else {
            Pair(
                interpolateValue(
                    config.initialScale,
                    config.finalScale,
                    config.scaleEasing.transform(clampedProgress)
                ).coerceAtLeast(0f),
                interpolateValue(
                    config.initialAlpha,
                    config.finalAlpha,
                    config.alphaEasing.transform(clampedProgress)
                ).coerceIn(0f, 1f)
            )
        }

        // Optimize color transition
        val color = if (colorTransitionEnabled) {
            config.colorTransition?.let { transition ->
                interpolateColorOptimized(
                    start = transition.startColor,
                    end = transition.endColor,
                    fraction = transition.easing.transform(clampedProgress)
                )
            } ?: particle.color
        } else {
            particle.color
        }

        // Optimize rotation calculation
        val rotation = normalizeRotation(particle.rotation + particle.angularVelocity)

        // Optimize trail update
        val trail = if (trailEnabled) {
            config.trailConfig?.let { trailConfig ->
                updateTrailOptimized(
                    currentTrail = particle.trail.map { TrailOffset(it, 1f) },
                    newPosition = particle.position,
                    config = trailConfig,
                    baseAlpha = alpha
                )
            } ?: emptyList()
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

    private fun normalizeRotation(rotation: Float): Float {
        return when {
            rotation < 0f -> 360f + (rotation % 360f)
            rotation >= 360f -> rotation % 360f
            else -> rotation
        }
    }

    private fun updateTrailOptimized(
        currentTrail: List<TrailOffset>,
        newPosition: Offset,
        config: ParticleConfig.TrailConfig,
        baseAlpha: Float
    ): List<TrailOffset> {
        trailList.clear()
        trailList.add(TrailOffset(newPosition, baseAlpha))

        val size = minOf(currentTrail.size + 1, config.length)
        val fadeStep = if (config.fadeOut) {
            (1f - config.minAlpha) / size
        } else {
            0f
        }

        for (i in 1 until size) {
            val trailPoint = currentTrail.getOrNull(i - 1) ?: break
            val trailAlpha = if (config.fadeOut) {
                (baseAlpha * (1f - (i.toFloat() * fadeStep))).coerceAtLeast(config.minAlpha)
            } else {
                baseAlpha
            }
            trailList.add(TrailOffset(trailPoint.position, trailAlpha))
        }

        return trailList.toList()
    }

    private fun interpolateValue(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    private fun interpolateColorOptimized(start: Color, end: Color, fraction: Float): Color {
        // Optimize for common cases
        if (fraction <= 0f) return start
        if (fraction >= 1f) return end

        return Color(
            red = start.red + (end.red - start.red) * fraction,
            green = start.green + (end.green - start.green) * fraction,
            blue = start.blue + (end.blue - start.blue) * fraction,
            alpha = start.alpha + (end.alpha - start.alpha) * fraction
        )
    }
}
