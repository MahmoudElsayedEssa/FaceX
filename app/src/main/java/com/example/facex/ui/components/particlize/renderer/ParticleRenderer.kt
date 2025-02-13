package com.example.test.new.renderer


import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.particleeffect.particlize.system.core.ParticleState

interface ParticleRenderer {
    fun DrawScope.renderParticle(particle: ParticleState.Active)
}
