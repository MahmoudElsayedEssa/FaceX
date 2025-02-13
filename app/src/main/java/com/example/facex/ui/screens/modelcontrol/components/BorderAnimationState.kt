package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.xr.runtime.math.lerp
import kotlin.math.PI
import kotlin.math.sin

private class BorderAnimationState {
    var animationValue by mutableFloatStateOf(0f)
}


@Composable
fun AnimatedBorder(
    enabled: Boolean = true, content: @Composable (Modifier) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val state = remember { BorderAnimationState() }

    // Enhanced color interpolation with better easing
    fun interpolateColors(color1: Color, color2: Color, steps: Int): List<Color> {
        return List(steps) { index ->
            // Using a custom easing curve for more elegant transitions
            val fraction = index.toFloat() / (steps - 1)
            val easedFraction = sin(fraction * PI * 0.5f).toFloat()
            Color(
                red = lerp(color1.red, color2.red, easedFraction),
                green = lerp(color1.green, color2.green, easedFraction),
                blue = lerp(color1.blue, color2.blue, easedFraction),
                alpha = lerp(0.9f, 1f, easedFraction) // Subtle alpha variation
            )
        }
    }

    // Create a refined color sequence with more sophisticated interpolation
    val primaryGradientColors = remember(colorScheme) {
        val baseColors = listOf(
            colorScheme.primary,
            colorScheme.secondary.copy(alpha = 0.95f),
            colorScheme.tertiary,
            colorScheme.primary.copy(alpha = 0.95f)
        )
        baseColors.zipWithNext().flatMap { (currentColor, nextColor) ->
            // Start with the main color
            listOf(currentColor) +
                    // Add transition colors to next main color
                    interpolateColors(currentColor, nextColor, 16).drop(1).dropLast(1)
            // Note: drop(1) and dropLast(1) to avoid duplicating the main colors
        }
    }


    // Refined rotation animation
    LaunchedEffect(enabled) {
        if (enabled) {
            animate(
                initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing), repeatMode = RepeatMode.Restart
                )
            ) { value, _ ->
                state.animationValue = value
            }
        }
    }

    val borderModifier = remember(enabled) {
        if (!enabled) {
            Modifier
        } else {
            Modifier.drawWithCache {
                    val strokeWidth = 1.5.dp.toPx()
                    val cornerRadius = CornerRadius(16.dp.toPx())
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Enhanced glow colors
                    val glowColors = primaryGradientColors.map { color ->
                        color.copy(alpha = 0.25f)
                    }

                    onDrawWithContent {
                        drawContent()

                        val baseStroke = strokeWidth * (1.4f)
                        val glowStroke = baseStroke * 3f

                        // Smooth gradient stops
                        val primaryStops = primaryGradientColors.mapIndexed { index, color ->
                            val fraction = index.toFloat() / (primaryGradientColors.size - 1)
                            val adjustedFraction = (fraction - state.animationValue) % 1f
                            adjustedFraction to color
                        }.sortedBy { it.first }.toTypedArray()

                        // Enhanced glow effect
                        val glowStops = glowColors.mapIndexed { index, color ->
                            val fraction = index.toFloat() / (glowColors.size - 1)
                            // Offset glow animation slightly for more interesting effect
                            val adjustedFraction = (fraction - state.animationValue - 0.35f) % 1f
                            adjustedFraction to color
                        }.sortedBy { it.first }.toTypedArray()

                        // Multi-layered glow for more depth
                        // Outer glow
                        drawRoundRect(
                            brush = Brush.sweepGradient(
                                colorStops = glowStops, center = center
                            ),
                            style = Stroke(width = glowStroke * 1.8f),
                            cornerRadius = cornerRadius,
                            alpha = 0.15f,
                            blendMode = BlendMode.Screen
                        )

                        // Middle glow
                        drawRoundRect(
                            brush = Brush.sweepGradient(
                                colorStops = glowStops, center = center
                            ),
                            style = Stroke(width = glowStroke * 1.4f),
                            cornerRadius = cornerRadius,
                            alpha = 0.2f,
                            blendMode = BlendMode.Screen
                        )

                        // Inner glow
                        drawRoundRect(
                            brush = Brush.sweepGradient(
                                colorStops = glowStops, center = center
                            ),
                            style = Stroke(width = glowStroke),
                            cornerRadius = cornerRadius,
                            alpha = 0.25f,
                            blendMode = BlendMode.Screen
                        )

                        // Main border with enhanced smoothness
                        drawRoundRect(
                            brush = Brush.sweepGradient(
                                colorStops = primaryStops, center = center
                            ), style = Stroke(
                                width = baseStroke, cap = StrokeCap.Round, join = StrokeJoin.Round
                            ), cornerRadius = cornerRadius
                        )
                    }
                }
        }
    }

    content(borderModifier)
}
