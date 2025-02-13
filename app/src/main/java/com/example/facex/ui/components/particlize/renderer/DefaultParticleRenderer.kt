package com.example.test.new.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import com.example.particleeffect.particlize.system.core.ParticleState


class CircleParticleRenderer : ParticleRenderer {
    override fun DrawScope.renderParticle(particle: ParticleState.Active) {
        drawCircle(
            color = Color(particle.color.toArgb()).copy(alpha = particle.alpha / 255f),
            radius = particle.radius,
            center = particle.position
        )
    }
}

class RectParticleRenderer : ParticleRenderer {
    override fun DrawScope.renderParticle(particle: ParticleState.Active) {
        val size = particle.radius * 2
        drawRect(
            color = Color(particle.color.toArgb()).copy(alpha = particle.alpha / 255f),
            topLeft = particle.position - Offset(particle.radius, particle.radius),
            size = Size(size, size)
        )
    }
}