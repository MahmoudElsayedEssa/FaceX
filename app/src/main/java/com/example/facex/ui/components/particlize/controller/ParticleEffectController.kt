package com.example.test.new.controller

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.example.particleeffect.particlize.controller.EffectLifecycle
import com.example.particleeffect.particlize.controller.EffectType
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.particleeffect.particlize.system.core.ParticleState
import com.example.particleeffect.particlize.system.engine.ParticleEngine
import com.example.particleeffect.particlize.system.generator.ParticleGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParticleEffectController(
    private val coroutineScope: CoroutineScope, private val config: ParticleConfig
) {
    private val _state = MutableStateFlow<EffectLifecycle>(EffectLifecycle.Idle)
    val state = _state.asStateFlow()

    private val _position = MutableStateFlow(Offset.Zero)
    private var effectLayer: GraphicsLayer? = null
    private var animationJob: Job? = null
    private var currentConfig: ParticleConfig? = null
    private val particleSystem = ParticleEngine(config = config)
    private var cachedParticles: List<ParticleState>? = null


    fun trigger(effectType: EffectType) {
//        if (_state.value !is EffectLifecycle.Idle && effectType != EffectType.ASSEMBLE) {
//            Log.w(TAG, "start() called but already running")
//            return
//        }

        Log.d(TAG, "Starting effect with config: $config")
        currentConfig = config
        animationJob?.cancel()

        animationJob = coroutineScope.launch(Dispatchers.Default) {
            try {
                Log.d(TAG, "State changed to Capturing")
                _state.value = EffectLifecycle.Capturing

                if (effectType == EffectType.DISINTEGRATE) {
                    Log.d(TAG, "Graphics layer found, capturing image")
                    effectLayer?.let { layer ->
                        val bitmap = layer.toImageBitmap()
                        val generator = ParticleGenerator(bitmap, config)
                        cachedParticles = generator.generateParticles()
                    }
                }
                if (cachedParticles == null) {
                    effectLayer?.let { layer ->
                        val bitmap = layer.toImageBitmap()
                        val generator = ParticleGenerator(bitmap, config)
                        cachedParticles = generator.generateParticles()
                    }
                }

                cachedParticles?.let { particles ->
                    particleSystem.generateParticleFlow(particles, effectType)
                        .flowOn(Dispatchers.Default).collectLatest { newParticles ->
                            withContext(Dispatchers.Main.immediate) {
                                if (newParticles.none { it is ParticleState.Active }) {
                                    _state.value = EffectLifecycle.Completed(effectType)
                                    currentConfig = null
                                } else {
                                    _state.value = EffectLifecycle.Animating(
                                        particles = newParticles
                                    )
                                }
                            }
                        }

                }

            } catch (e: Exception) {
                Log.e(TAG, "Animation error: ", e)
                reset()
            }
        }
    }


    private fun reset() {
        Log.d(TAG, "Resetting effect state")
        animationJob?.cancel()
        _state.value = EffectLifecycle.Idle
        currentConfig = null
        effectLayer = null
    }

    fun setGraphicsLayer(graphicsLayer: GraphicsLayer) {
        Log.d(TAG, "Graphics layer set")
        effectLayer = graphicsLayer
    }

    fun updatePosition(newPosition: Offset) {
        Log.d(TAG, "Position updated: $newPosition")
        _position.value = newPosition
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up resources")
        reset()
        coroutineScope.cancel()
    }

    companion object {
        private const val TAG = "ParticleEffectController"
    }
}


@Composable
fun rememberParticleEffectController(config: ParticleConfig): ParticleEffectController {
    val scope = rememberCoroutineScope()
    return remember(config) {
        ParticleEffectController(
            coroutineScope = scope, config = config
        )
    }
}