package com.example.facex.ui.screens.modelcontrol.components

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.facex.ui.components.icons.filled.Rocket
import com.example.particleeffect.particlize.controller.EffectType
import com.example.particleeffect.particlize.system.core.ParticleConfig
import com.example.test.new.compose.particleEffect
import com.example.test.new.controller.rememberParticleEffectController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RunButton(
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val particleConfig = remember { ParticleConfig.default }
    val controller = rememberParticleEffectController(config = particleConfig)

    val rocketPainter = rememberVectorPainter(Rocket)
    val colorScheme = MaterialTheme.colorScheme
    val tintColor = remember(colorScheme) { colorScheme.primary }

    // Rocket animation states
    val rocketLaunchProgress = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val rocketHover by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        )
    )

    // Rocket animation parameters
    val rocketOffset by animateDpAsState(
        targetValue = if (rocketLaunchProgress.value > 0) (-32).dp else 0.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    val rocketRotation by animateFloatAsState(
        targetValue = if (rocketLaunchProgress.value > 0) 15f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    val rocketScale by animateFloatAsState(
        targetValue = if (rocketLaunchProgress.value > 0) 1.5f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )


    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.particleEffect(
                controller = controller,
            )
    ) {
        FilledTonalButton(
            onClick = {
                scope.launch {
                    isPressed = true
                    rocketLaunchProgress.snapTo(0f)
                    rocketLaunchProgress.animateTo(
                        targetValue = 1f, animationSpec = tween(300, easing = LinearEasing)
                    )

                    try {
                        controller.trigger(
                            effectType = EffectType.DISINTEGRATE,
                        )
                    } catch (e: Exception) {
                        Log.e("CurrentLabel", "Failed to trigger disintegration", e)
                    }

                    delay(150)
                    onClick()
                    delay(300)
                    isPressed = false
                    rocketLaunchProgress.snapTo(0f)
                }
            }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(
                horizontal = 16.dp, vertical = 8.dp
            ), elevation = ButtonDefaults.filledTonalButtonElevation(
                defaultElevation = 1.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 2.dp,
                focusedElevation = 1.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Box(modifier = Modifier
                    .graphicsLayer {
                        translationY = 2.dp.toPx() * rocketHover // Reduced hover
                        rotationZ = rocketRotation
                        scaleX = rocketScale
                        scaleY = rocketScale
                    }
                    .offset(y = rocketOffset)) {


                    Box(modifier = Modifier
                        .size(18.dp)
                        .drawBehind {
                            with(rocketPainter) {
                                draw(size, colorFilter = ColorFilter.tint(tintColor))
                            }
                        })
                }

                Text(
                    "Run",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


