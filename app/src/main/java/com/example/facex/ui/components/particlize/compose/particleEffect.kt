package com.example.test.new.compose


import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.example.particleeffect.particlize.controller.EffectLifecycle
import com.example.particleeffect.particlize.controller.EffectType
import com.example.particleeffect.particlize.system.core.ParticleState
import com.example.test.new.controller.ParticleEffectController
import com.example.test.new.renderer.ParticleRenderer
import com.example.test.new.renderer.RectParticleRenderer


fun Modifier.particleEffect(
    controller: ParticleEffectController,
    renderer: ParticleRenderer = RectParticleRenderer(),
    block: GraphicsLayer.() -> Unit = {}
): Modifier = composed {
    val graphicsLayer = rememberGraphicsLayer().apply {
        clip = true
        block()
    }

    val currentState by controller.state.collectAsState()

    // Use remember to cache the draw scope

    LaunchedEffect(Unit) {
        controller.setGraphicsLayer(graphicsLayer)
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.cleanup()
        }
    }

    Modifier
        .drawWithContent {
            when (val state = currentState) {
                is EffectLifecycle.Idle, is EffectLifecycle.Capturing -> {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }

                is EffectLifecycle.Animating -> {

                    // Batch process particles for better performance
                    val activeParticles =
                        state.particles.asSequence().filterIsInstance<ParticleState.Active>()

                    // Use sequence operations for better memory efficiency
                    activeParticles.chunked(100).forEach { batch ->
                        batch.forEach { particle ->
                            try {
                                with(renderer) {
                                    renderParticle(particle)
                                }
                            } catch (e: Exception) {
                                // Log error but continue processing
                            }
                        }
                    }
                }

                is EffectLifecycle.Completed -> {
                    if (state.effectType == EffectType.ASSEMBLE) {
                        drawContent()
                    }
                }
            }
        }
        .onGloballyPositioned { coordinates ->
            controller.updatePosition(coordinates.positionInWindow())
        }
}



