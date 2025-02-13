package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun CharacterWithBlur(
    char: Char,
    color: Color,
    delayMillis: Long,
    modifier: Modifier = Modifier
) {
    val blur = remember { Animatable(8f) }
    val offset = remember { Animatable(4f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(char) {
        delay(delayMillis)

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            blur.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            offset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box {
        // Glow effect
        Text(
            text = char.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = color.copy(alpha = 0.3f),
            modifier = modifier
                .graphicsLayer {
                    this.alpha = alpha.value * 0.5f
                }
                .offset(y = offset.value.dp)
                .blur(16.dp)
        )

        // Main character
        Text(
            text = char.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = modifier
                .graphicsLayer {
                    this.alpha = alpha.value
                }
                .offset(y = offset.value.dp)
                .blur(blur.value.dp)
        )
    }
}
