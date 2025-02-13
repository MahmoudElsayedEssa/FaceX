package com.example.particleeffect.particlize.system.physics

import androidx.compose.ui.geometry.Offset
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration

class DisintegrationPhysics {
    // Pre-calculated constants
    private val baseVelocityScale = 0.5f
    private val gravityScale = 0.15f
    private val turbulenceScale = 0.8f
    private val attractionScale = 0.1f
    private val positionScale = 1.5f
    private val noiseScale = 0.05f
    private val randomScale = 0.5f
    private val pi2 = 2f * PI.toFloat()

    private fun generateInitialVelocity(config: ParticleConfig): Offset {
        val baseSpeed = config.motion.initialVelocity * baseVelocityScale

        val baseAngle = when (val pattern = config.emission.pattern) {
            is ParticleConfig.EmissionPattern.Line -> {
                pattern.angle * (PI / 180f)
            }

            is ParticleConfig.EmissionPattern.Radial -> {
                Random.nextFloat() * pi2
            }

            else -> -PI / 2f
        }.toFloat()

        val spread = if (config.motion.randomizeDirection) {
            Random.nextFloat() * pi2
        } else {
            ((Random.nextFloat() - 0.5f) * config.motion.spread * (PI / 180f)).toFloat()
        }

        val finalAngle = baseAngle + spread
        val randomSpeed = baseSpeed * (0.8f + Random.nextFloat() * 0.6f)

        return Offset(
            x = (cos(finalAngle) * randomSpeed),
            y = (sin(finalAngle) * randomSpeed)
        )
    }

    private fun calculateNewPosition(
        position: Offset,
        velocity: Offset,
        deltaSeconds: Float
    ): Offset {
        return position + (velocity * deltaSeconds * positionScale)
    }

    // Rest of the ParticlePhysics class implementation remains the same...
    fun updateParticle(
        particle: ParticleState.Active,
        config: ParticleConfig,
        deltaTime: Duration
    ): Pair<Offset, Offset> {
        val deltaSeconds = deltaTime.inWholeMilliseconds / 1000f

        var newVelocity = if (particle.velocity == Offset.Zero) {
            generateInitialVelocity(config)
        } else {
            particle.velocity
        }

        newVelocity = applyForcesOptimized(
            velocity = newVelocity,
            particle = particle,
            config = config.motion,
            deltaSeconds = deltaSeconds
        )

        val newPosition = calculateNewPosition(
            position = particle.position,
            velocity = newVelocity,
            deltaSeconds = deltaSeconds
        )

        return newPosition to newVelocity
    }

    private fun applyForcesOptimized(
        velocity: Offset,
        particle: ParticleState.Active,
        config: ParticleConfig.MotionConfig,
        deltaSeconds: Float
    ): Offset {
        var newVelocity = velocity

        // Combine multiple force calculations
        if (config.gravity != 0f || config.drag != 0f) {
            val gravityForce = if (config.gravity != 0f) {
                config.gravity * gravityScale * deltaSeconds
            } else 0f

            val dragFactor = if (config.drag != 0f) {
                1f - config.drag.coerceIn(0f, 1f)
            } else 1f

            newVelocity = Offset(
                x = newVelocity.x * dragFactor,
                y = (newVelocity.y * dragFactor) + gravityForce
            )
        }

        if (config.rotationSpeed != 0f) {
            val angle = config.rotationSpeed * deltaSeconds
            val cos = cos(angle)
            val sin = sin(angle)
            newVelocity = Offset(
                x = newVelocity.x * cos - newVelocity.y * sin,
                y = newVelocity.x * sin + newVelocity.y * cos
            )
        }

        config.turbulence.let { turbulence ->
            if (turbulence.amplitude != 0f) {
                val turbForce = generateOptimizedTurbulence(turbulence, particle.position)
                newVelocity += turbForce * turbulenceScale * deltaSeconds
            }
        }

        config.attractionPoint?.let { attraction ->
            if (attraction.strength != 0f) {
                val attractForce = calculateOptimizedAttractionForce(particle.position, attraction)
                newVelocity += attractForce * attractionScale * deltaSeconds
            }
        }

        return newVelocity
    }

    private fun generateOptimizedTurbulence(
        config: ParticleConfig.TurbulenceConfig,
        position: Offset
    ): Offset {
        val time = System.nanoTime() / 1_000_000_000f
        val scale = noiseScale * config.frequency

        val (x, y) = SimplexNoise.noise2D(
            position.x * scale,
            position.y * scale,
            time
        )

        return Offset(x - 0.5f, y - 0.5f) * config.amplitude
    }

    private fun calculateOptimizedAttractionForce(
        position: Offset,
        config: ParticleConfig.AttractionConfig
    ): Offset {
        val direction = config.point - position
        val distance = direction.getDistance().coerceAtLeast(0.1f)

        return if (distance <= config.radius) {
            direction * ((1f - distance / config.radius) * config.strength / distance)
        } else {
            Offset.Zero
        }
    }

    private fun Offset.normalize(): Offset {
        val distance = getDistance()
        return if (distance > 0) this / distance else Offset.Zero
    }

}
