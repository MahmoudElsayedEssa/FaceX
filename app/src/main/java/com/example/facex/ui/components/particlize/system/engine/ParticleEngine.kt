package com.example.particleeffect.particlize.system.engine

import com.example.particleeffect.particlize.controller.EffectType
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleState
import com.example.particleeffect.particlize.system.physics.AssemblePhysics
//import com.example.particleeffect.particlize.system.physics.AssemblePhysics
import com.example.particleeffect.particlize.system.physics.DisintegrationPhysics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ParticleEngine(private val config: ParticleConfig) {
    private val disintegrationPhysics = DisintegrationPhysics()
    private val assemblePhysics = AssemblePhysics()
    private val disintegrationAppearanceUpdater = DisintegrationAppearanceUpdater(config.appearance)
    private val assembleAppearanceUpdater = AssembleAppearanceUpdater(config.appearance)

    fun generateParticleFlow(
        initialParticles: List<ParticleState>,
        effectType: EffectType
    ): Flow<List<ParticleState>> = flow {
        val startTime = System.nanoTime()
        var lastTime = startTime
        var particles = initialParticles
        val lifetimeMs = config.emission.lifetime.inWholeMilliseconds.toFloat()
        val hasLifetime = lifetimeMs > 0

        while (particles.any { it is ParticleState.Active }) {
            val now = System.nanoTime()
            val deltaTime = (now - lastTime).toDuration(DurationUnit.NANOSECONDS)
            val globalProgress =
                ((now - startTime).toDouble() / config.animation.duration.inWholeNanoseconds).toFloat()

            particles = particles.map { particle ->
                if (particle is ParticleState.Active)
                    when (effectType) {
                        EffectType.ASSEMBLE -> {
                            updateAssembleParticle(
                                particle,
                                globalProgress,
                                deltaTime,
                                hasLifetime,
                                lifetimeMs
                            )

                        }

                        EffectType.DISINTEGRATE -> {
                            updateDisintegrationParticle(
                                particle,
                                globalProgress,
                                deltaTime,
                                hasLifetime,
                                lifetimeMs
                            )

                        }
                    }
                else particle
            }
            emit(particles)
            lastTime = now
            delay(16)
        }
    }


    private fun updateAssembleParticle(
        particle: ParticleState.Active,
        globalProgress: Float,
        deltaTime: Duration,
        hasLifetime: Boolean,
        lifetimeMs: Float
    ): ParticleState {
        val (newPosition, newVelocity) = assemblePhysics.updateParticle(
            particle = particle,
            config = config,
            deltaTime = deltaTime
        )

        // Update life progress
        val progressIncrement = deltaTime.inWholeMilliseconds.toFloat() / lifetimeMs
        val lifeProgress = if (hasLifetime) {
            (particle.lifeProgress + progressIncrement).coerceIn(0f, 1f)
        } else 0f

        if (lifeProgress >= 1f) return ParticleState.Dead

        // Use the specialized appearance updater
        val appearance = assembleAppearanceUpdater.updateAppearance(particle, globalProgress)

        return particle.copy(
            position = newPosition,
            velocity = newVelocity,
            scale = appearance.scale,
            rotation = appearance.rotation,
            progress = globalProgress,
            alpha = appearance.alpha,
            color = appearance.color,
            lifeProgress = lifeProgress,
            trail = appearance.trail.map { it.position }
        )
    }

    private fun updateDisintegrationParticle(
        particle: ParticleState.Active,
        globalProgress: Float,
        deltaTime: Duration,
        hasLifetime: Boolean,
        lifetimeMs: Float
    ): ParticleState {
        // Choose the physics update based on the effect type.
        val (newPosition, newVelocity) = disintegrationPhysics.updateParticle(
            particle,
            config,
            deltaTime
        )

        // Update life progress.
        val progressIncrement = deltaTime.inWholeMilliseconds.toFloat() / lifetimeMs
        val lifeProgress = if (hasLifetime)
            (particle.lifeProgress + progressIncrement).coerceIn(0f, 1f)
        else 0f

        if (lifeProgress >= 1f) return ParticleState.Dead


        // Update appearance (using the provided implementation).
        val appearance = disintegrationAppearanceUpdater.updateAppearance(particle, lifeProgress)

        return particle.copy(
            position = newPosition,
            velocity = newVelocity,
            scale = appearance.scale,
            rotation = appearance.rotation,
            progress = globalProgress,
            alpha = appearance.alpha,
            color = appearance.color,
            radius = particle.radius * randomSize(),
            lifeProgress = lifeProgress,
            trail = appearance.trail.map { it.position }
        )
    }

    private fun randomSize(): Float = Random.nextDouble(0.6, 1.1).toFloat()
}


//class ParticleEngine(private val config: ParticleConfig) {
//    private val physics = DisintegrationPhysics()
//    private val appearanceUpdater = ParticleAppearanceUpdater(config.appearance)
//
//
//    fun generateParticleFlow(
//        initialParticles: List<ParticleState>,
//        effectType: EffectType
//    ): Flow<List<ParticleState>> = flow {
//        val startTime = System.nanoTime()
//        var currentParticles = ArrayList<ParticleState>(initialParticles.size)
//        currentParticles.addAll(initialParticles)
//
//        var lastUpdateTime = System.nanoTime()
//
//        // Pre-calculate common values
//        val lifetimeMs = config.emission.lifetime.inWholeMilliseconds.toFloat()
//        val hasLifetime = lifetimeMs > 0
//
//        while (currentParticles.any { it is ParticleState.Active }) {
//            val currentTime = System.nanoTime()
//            val deltaTime = (currentTime - lastUpdateTime).toDuration(DurationUnit.NANOSECONDS)
//            val globalProgress =
//                ((currentTime - startTime).toDouble() / config.animation.duration.inWholeNanoseconds).toFloat()
//
//            // Update particles in batches for better performance
//            val updatedParticles = ArrayList<ParticleState>(currentParticles.size)
//
//            currentParticles.forEach { particle ->
//                when (particle) {
//                    is ParticleState.Active -> {
//                        val updated = updateParticleOptimized(
//                            particle,
//                            globalProgress,
//                            deltaTime,
//                            hasLifetime,
//                            lifetimeMs,
//                        )
//                        updatedParticles.add(updated)
//                    }
//
//                    ParticleState.Dead -> updatedParticles.add(particle)
//                }
//            }
//
//            currentParticles = updatedParticles
//            emit(currentParticles)
//
//            lastUpdateTime = currentTime
//            delay(16)
//        }
//    }
//
//    private fun updateParticleOptimized(
//        particle: ParticleState.Active,
//        globalProgress: Float,
//        deltaTime: Duration,
//        hasLifetime: Boolean,
//        lifetimeMs: Float,
//
//    ): ParticleState {
//        // Physics update
//        val (newPosition, newVelocity) = physics.updateParticle(
//            particle = particle,
//            config = config,
//            deltaTime = deltaTime
//        )
//
//        // Life progress calculation
//        val lifeProgress = if (hasLifetime) {
//            (particle.lifeProgress + (deltaTime.inWholeMilliseconds.toFloat() / lifetimeMs)).coerceIn(
//                0f, 1f
//            )
//        } else 0f
//
//        if (lifeProgress >= 1f) {
//            return ParticleState.Dead
//        }
//
//        // Appearance update
//        val appearance = appearanceUpdater.updateAppearance(particle, lifeProgress)
//
//        return particle.copy(position = newPosition,
//            velocity = newVelocity,
//            scale = appearance.scale,
//            rotation = appearance.rotation,
//            progress = globalProgress,
//            alpha = appearance.alpha,
//            color = appearance.color,
//            radius = particle.radius * randomSize(),
//            lifeProgress = lifeProgress,
//            trail = appearance.trail.map { it.position })
//    }
//
//    private fun randomSize(): Float {
//        return Random.nextDouble(0.6, 1.15).toFloat()
//    }
//}
