package com.example.particleeffect.particlize.controller

import com.example.particleeffect.particlize.system.core.ParticleState

sealed class EffectLifecycle {
    data object Idle : EffectLifecycle()
    data object Capturing : EffectLifecycle()
    data class Animating(val particles: List<ParticleState>) : EffectLifecycle()
    data class Completed(val effectType: EffectType) : EffectLifecycle()
}
