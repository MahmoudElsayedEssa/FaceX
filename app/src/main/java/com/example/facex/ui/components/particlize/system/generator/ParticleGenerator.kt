package com.example.particleeffect.particlize.system.generator

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleState
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration


class ParticleGenerator(
    private val image: ImageBitmap,
    private val config: ParticleConfig
) {
    private var nextParticleId = AtomicLong(0)

    // Pre-calculate constants
    private val randomAlphas = listOf(0.2f, 0.7f, 1.0f)
    private val piDivided180 = PI.toFloat() / 180f
    private val pi2 = 2f * PI.toFloat()

    // Reusable buffer for pixel data
    private val buffer by lazy { IntArray(image.width * image.height) }

    fun generateParticles(): List<ParticleState.Active> {
        // Use ArrayDeque for better performance with add operations
        val particles = ArrayDeque<ParticleState.Active>(
            (image.width * image.height) / (config.particleDensity * config.particleDensity)
        )

        image.readPixels(buffer, 0, 0, image.width, image.height)

        // Pre-calculate common values
        val imageWidth = image.width
        val imageHeight = image.height
        val density = config.particleDensity
        val staggerConfig = config.emission.stagger

        // Create particle batches for better memory efficiency
        val positions = mutableListOf<Triple<Int, Int, Int>>()

        for (x in 0 until imageWidth step density) {
            for (y in 0 until imageHeight step density) {
                val index = y * imageWidth + x
                val pixel = buffer[index]
                val alpha = (pixel shr 24) and 0xFF

                if (alpha >= 20) {
                    positions.add(Triple(x, y, pixel))
                }
            }
        }

        // Process particles in parallel for large datasets
        if (positions.size > 1000) {
            positions.chunked(1000).forEach { batch ->
                batch.mapTo(particles) { (x, y, pixel) ->
                    createParticleFromPosition(
                        x,
                        y,
                        pixel,
                        imageWidth,
                        imageHeight,
                        density,
                        staggerConfig
                    )
                }
            }
        } else {
            positions.mapTo(particles) { (x, y, pixel) ->
                createParticleFromPosition(
                    x,
                    y,
                    pixel,
                    imageWidth,
                    imageHeight,
                    density,
                    staggerConfig
                )
            }
        }

        return particles
    }

    private fun createParticleFromPosition(
        x: Int,
        y: Int,
        pixel: Int,
        imageWidth: Int,
        imageHeight: Int,
        density: Int,
        staggerConfig: ParticleConfig.StaggerConfig
    ): ParticleState.Active {
        val color = Color(pixel)
        val position = Offset(x.toFloat(), y.toFloat())

        val staggerDelay = calculateStaggerDelay(
            x, y, density, staggerConfig, imageWidth, imageHeight
        )

        return createParticle(
            position = position,
            color = color,
            alpha = randomInitialAlpha(pixel),
            staggerDelay = staggerDelay
        )
    }

    private fun randomInitialAlpha(pixel: Int): Float {
        val colorAlpha = (pixel shr 24 and 0xFF) / 255f
        return minOf(colorAlpha, randomAlphas.random()) * 100
    }

    private fun createParticle(
        position: Offset,
        color: Color,
        alpha: Float,
        staggerDelay: Duration
    ): ParticleState.Active {
        val velocity = calculateInitialVelocity(position)
        val initialLifeProgress = if (config.emission.lifetime != Duration.ZERO) {
            staggerDelay.inWholeMilliseconds.toFloat() / config.emission.lifetime.inWholeMilliseconds.toFloat()
        } else 0f

        return ParticleState.Active(
            id = nextParticleId.getAndIncrement(),
            position = position,
            velocity = velocity,
            scale = config.appearance.initialScale,
            rotation = if (config.motion.rotationSpeed != 0f) Random.nextFloat() * 360f else 0f,
            angularVelocity = config.motion.rotationSpeed,
            alpha = alpha * config.appearance.initialAlpha,
            color = color,
            progress = 0f,
            mass = 1f,
            lifeProgress = initialLifeProgress,
            distanceFromOrigin = 0f,
            trail = emptyList(),
            radius = 8f * config.appearance.initialScale,
            targetPosition = position,
        )
    }

    private fun calculateInitialVelocity(position: Offset): Offset {
        val motion = config.motion
        val angleRadians = when (val pattern = config.emission.pattern) {
            is ParticleConfig.EmissionPattern.Radial -> {
                atan2(position.y, position.x)
            }

            is ParticleConfig.EmissionPattern.Line -> {
                val baseAngle = pattern.angle * piDivided180
                (baseAngle + (Random.nextFloat() * pattern.spread - pattern.spread / 2f) * piDivided180).toFloat()
            }

            is ParticleConfig.EmissionPattern.Burst -> {
                atan2(position.y, position.x)
            }

            else -> {
                if (motion.randomizeDirection) Random.nextFloat() * pi2 else 0f
            }
        }

        return Offset(
            x = (motion.initialVelocity * cos(angleRadians)),
            y = (motion.initialVelocity * sin(angleRadians))
        )
    }

    private fun calculateStaggerDelay(
        x: Int,
        y: Int,
        gridSpacing: Int,
        config: ParticleConfig.StaggerConfig,
        imageWidth: Int,
        imageHeight: Int
    ): Duration {
        return when (config.mode) {
            ParticleConfig.StaggerConfig.StaggerMode.TopToBottom -> {
                config.delayPerParticle * (y / gridSpacing)
            }

            ParticleConfig.StaggerConfig.StaggerMode.BottomToTop -> {
                val maxRow = (imageHeight - 1) / gridSpacing
                config.delayPerParticle * (maxRow - y / gridSpacing)
            }

            ParticleConfig.StaggerConfig.StaggerMode.Random -> {
                config.delayPerParticle * Random.nextDouble()
            }

            ParticleConfig.StaggerConfig.StaggerMode.InsideOut,
            ParticleConfig.StaggerConfig.StaggerMode.OutsideIn -> {
                calculateRadialStaggerDelay(x, y, imageWidth, imageHeight, config)
            }
        }
    }

    private fun calculateRadialStaggerDelay(
        x: Int,
        y: Int,
        imageWidth: Int,
        imageHeight: Int,
        config: ParticleConfig.StaggerConfig
    ): Duration {
        val centerX = imageWidth / 2f
        val centerY = imageHeight / 2f
        val maxDistance = sqrt((imageWidth / 2.0).pow(2) + (imageHeight / 2.0).pow(2))
        val distance = sqrt((x - centerX).pow(2) + (y - centerY).pow(2))

        return if (config.mode == ParticleConfig.StaggerConfig.StaggerMode.InsideOut) {
            config.delayPerParticle * distance.toDouble()
        } else {
            config.delayPerParticle * (maxDistance - distance)
        }
    }
}
