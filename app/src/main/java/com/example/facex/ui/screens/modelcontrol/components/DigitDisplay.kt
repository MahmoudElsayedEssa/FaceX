package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun DigitDisplay(
    digit: Digit,
    blurRadius: Float,
    scaleFrom: Float,
    scaleTo: Float
) {
    Box(
        modifier = Modifier.padding(horizontal = 0.5.dp),
        contentAlignment = Alignment.Center
    ) {
        var isBlurring by remember { mutableStateOf(true) }
        val blur = remember { Animatable(blurRadius) }
        val scale = remember { Animatable(scaleFrom) }

        LaunchedEffect(digit) {
            blur.snapTo(blurRadius)
            scale.snapTo(scaleFrom)
            isBlurring = true

            launch {
                delay(30)
                blur.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            launch {
                delay(30)
                scale.animateTo(
                    targetValue = scaleTo,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            delay(200)
            isBlurring = false
        }

        // Glow effect
        if (isBlurring && digit.digitChar.isDigit()) {
            Text(
                text = digit.digitChar.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .scale(1.1f)
                    .blur(8.dp)
            )
        }

        // Main digit
        Text(
            text = digit.digitChar.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .blur(blur.value.dp)
        )
    }
}


data class Digit(
    val digitChar: Char,
    val fullNumber: Int,
    val place: Int
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Digit -> digitChar == other.digitChar && place == other.place
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = digitChar.hashCode()
        result = 31 * result + place
        return result
    }
}

operator fun Digit.compareTo(other: Digit): Int {
    return when {
        this.place == other.place -> fullNumber.compareTo(other.fullNumber)
        else -> place.compareTo(other.place)
    }
}