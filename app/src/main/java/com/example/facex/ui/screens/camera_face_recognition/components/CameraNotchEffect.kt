package com.example.facex.ui.screens.camera_face_recognition.components

import android.os.Build
import android.view.DisplayCutout
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun CameraNotchEffect(
    cutout: DisplayCutout,
    modifier: Modifier = Modifier
) {
    val colors = object {
        val primary = MaterialTheme.colorScheme.primary
        val accent = MaterialTheme.colorScheme.surfaceTint
        val highlight = MaterialTheme.colorScheme.inversePrimary

        val arcColor = primary.copy(alpha = 0.8f)
        val gridColor = accent.copy(alpha = 0.08f)
        val dotColor = highlight.copy(alpha = 0.9f)
        val dotGlowColor = highlight.copy(alpha = 0.2f)
    }

    val notchBounds = cutout.boundingRects.firstOrNull()

    if (notchBounds != null) {
        val centerX = notchBounds.centerX().toFloat()
        val cameraRadius = notchBounds.width() / 2f
        val centerY = (notchBounds.bottom - cameraRadius)
        val effectRadius = cameraRadius * 2f

        val infiniteTransition = rememberInfiniteTransition(label = "camera")

        // Main animation progress
        val arcProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000,
                    easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "arc"
        )

        // Dot scale animation
        val dotScale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500,
                    easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dots"
        )

        // Dot glow animation
        val dotGlow by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )

        // Calculate visibility states
        val arcVisibility by remember {
            derivedStateOf {
                val meetingPoint = 0.5f
                val fadeRange = 0.1f
                when {
                    arcProgress < meetingPoint - fadeRange -> 1f
                    arcProgress < meetingPoint ->
                        cos((arcProgress - (meetingPoint - fadeRange)) / fadeRange * PI.toFloat() / 2)
                    else -> 0f
                }
            }
        }

        val circleVisibility by remember {
            derivedStateOf {
                when {
                    arcProgress < 0.5f -> 0f
                    arcProgress < 0.7f ->
                        sin((arcProgress - 0.5f) / 0.2f * PI.toFloat() / 2)
                    arcProgress < 0.8f -> 1f
                    arcProgress < 1f ->
                        cos((arcProgress - 0.8f) / 0.2f * PI.toFloat() / 2)
                    else -> 0f
                }
            }
        }

        Canvas(modifier = modifier.fillMaxSize()) {
            // Draw dots
            val points = listOf(
                Pair(-0.5f, -0.5f), // Left dot
                Pair(0.5f, -0.5f),  // Right dot
            )

            points.forEach { (x, y) ->
                val dotCenter = Offset(
                    centerX + (x * effectRadius * 0.9f),
                    centerY + (y * effectRadius * 0.9f)
                )

                // Draw dot glow
                drawCircle(
                    color = colors.dotGlowColor.copy(alpha = 0.3f * dotGlow),
                    center = dotCenter,
                    radius = 6.dp.toPx() * dotScale,
                    style = Fill
                )

                // Draw main dot
                drawCircle(
                    color = colors.dotColor,
                    center = dotCenter,
                    radius = 2.5.dp.toPx() * dotScale,
                    style = Fill
                )
            }

            // Grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val progress = (arcProgress + i.toFloat() / gridLines) % 1f
                val yOffset = (progress * effectRadius * 2) - effectRadius

                val distanceFromCenter = abs(yOffset) / effectRadius
                val gridAlpha = (1f - distanceFromCenter) * 0.5f

                if (arcProgress < 0.7f) {
                    drawLine(
                        color = colors.gridColor.copy(alpha = gridAlpha),
                        start = Offset(
                            centerX - sqrt((effectRadius * effectRadius - yOffset * yOffset).coerceAtLeast(0f)),
                            centerY + yOffset
                        ),
                        end = Offset(
                            centerX + sqrt((effectRadius * effectRadius - yOffset * yOffset).coerceAtLeast(0f)),
                            centerY + yOffset
                        ),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }

            // Outer circle
            if (circleVisibility > 0f) {
                drawCircle(
                    color = colors.arcColor.copy(alpha = 0.8f * circleVisibility),
                    center = Offset(centerX, centerY),
                    radius = effectRadius,
                    style = Stroke(
                        width = 2.dp.toPx() * (0.9f + (circleVisibility * 0.2f))
                    )
                )
            }

            // Arcs
            if (arcVisibility > 0f) {
                val baseArcWidth = 2.dp.toPx()
                val arcLength = 45f
                val baseAngle = 360 * arcProgress

                // First arc (clockwise)
                drawArc(
                    color = colors.arcColor.copy(alpha = 0.8f * arcVisibility),
                    startAngle = baseAngle,
                    sweepAngle = arcLength,
                    useCenter = false,
                    topLeft = Offset(centerX - effectRadius, centerY - effectRadius),
                    size = Size(effectRadius * 2, effectRadius * 2),
                    style = Stroke(width = baseArcWidth, cap = StrokeCap.Round)
                )

                // Second arc (counter-clockwise)
                drawArc(
                    color = colors.arcColor.copy(alpha = 0.8f * arcVisibility),
                    startAngle = -baseAngle,
                    sweepAngle = arcLength,
                    useCenter = false,
                    topLeft = Offset(centerX - effectRadius, centerY - effectRadius),
                    size = Size(effectRadius * 2, effectRadius * 2),
                    style = Stroke(width = baseArcWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}

