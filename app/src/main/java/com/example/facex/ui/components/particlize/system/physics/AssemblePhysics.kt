// AssemblePhysics.kt
package com.example.particleeffect.particlize.system.physics


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration

class AssemblePhysics {
    // Initial scatter configuration
    private val scatterRadius = 1000f  // How far particles initially scatter
    private val scatterAngleRange = 2f * PI.toFloat()

    fun updateParticle(
        particle: ParticleState.Active,
        config: ParticleConfig,
        deltaTime: Duration
    ): Pair<Offset, Offset> {
        val deltaSeconds = deltaTime.inWholeMilliseconds / 1000f

        // If this is the first update and the particle is at its target, scatter it
        if (particle.position == particle.targetPosition) {
            return scatterParticle(particle)
        }

        // Calculate direction to target
        val direction = particle.targetPosition - particle.position
        val distance = direction.getDistance()

        // Normalized direction vector
        val normalizedDirection = if (distance > 0f) {
            direction / distance
        } else {
            Offset.Zero
        }

        // Calculate speed based on distance from target
        // Particles move faster when they're further away
        val speed = (BASE_SPEED + distance * DISTANCE_SPEED_FACTOR) *
                config.motion.initialVelocity

        // Add some turbulence for natural movement
        val turbulence = generateTurbulence(
            config.motion.turbulence,
            particle.position,
            particle.progress
        )

        // Calculate new velocity
        val newVelocity = (normalizedDirection * speed) + turbulence

        // Apply easing as particles get closer to their target
        val easingFactor = (distance / scatterRadius).coerceIn(0f, 1f)

        // Calculate new position
        val newPosition = calculateNewPosition(
            particle.position,
            newVelocity,
            deltaSeconds,
            particle.targetPosition,
            easingFactor
        )

        return newPosition to newVelocity
    }

    private fun scatterParticle(particle: ParticleState.Active): Pair<Offset, Offset> {
        // Generate random angle and distance for initial scatter
        val angle = Random.nextFloat() * scatterAngleRange
        val distance = Random.nextFloat() * scatterRadius

        // Calculate scattered position
        val scatteredPosition = Offset(
            x = particle.targetPosition.x + cos(angle) * distance,
            y = particle.targetPosition.y + sin(angle) * distance
        )

        // Initial velocity towards target
        val direction = (particle.targetPosition - scatteredPosition).normalize()
        val initialVelocity = direction * BASE_SPEED

        return scatteredPosition to initialVelocity
    }

    private fun calculateNewPosition(
        currentPosition: Offset,
        velocity: Offset,
        deltaSeconds: Float,
        targetPosition: Offset,
        easingFactor: Float
    ): Offset {
        // Basic movement
        val baseMovement = velocity * deltaSeconds

        // Apply stronger movement when far from target, more precise when close
        val newPosition = currentPosition + (baseMovement * easingFactor)

        // Smooth interpolation towards target as particles get closer
        return lerp(
            newPosition,
            targetPosition,
            (1f - easingFactor) * LERP_STRENGTH
        )
    }

    private fun generateTurbulence(
        config: ParticleConfig.TurbulenceConfig,
        position: Offset,
        progress: Float
    ): Offset {
        val time = System.nanoTime() / 1_000_000_000f
        val scale = NOISE_SCALE * config.frequency

        // Reduce turbulence as particles get closer to their target
        val turbulenceStrength = config.amplitude * (1f - progress)

        val x = SimplexNoise.noise(
            position.x * scale,
            position.y * scale,
            time
        )
        val y = SimplexNoise.noise(
            position.x * scale + 1000,
            position.y * scale + 1000,
            time
        )

        return Offset(x - 0.5f, y - 0.5f) * turbulenceStrength
    }


    private fun Offset.normalize(): Offset {
        val magnitude = getDistance()
        return if (magnitude > 0) this / magnitude else this
    }

    companion object {
        private const val BASE_SPEED = 0.05f
        private const val DISTANCE_SPEED_FACTOR = 0.08f
        private const val NOISE_SCALE = 0.05f
        private const val LERP_STRENGTH = 0.1f
    }
}

