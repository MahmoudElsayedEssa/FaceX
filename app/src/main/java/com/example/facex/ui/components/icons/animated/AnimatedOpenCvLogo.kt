package com.example.facex.ui.components.icons.animated


import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
private fun OpenCvShape(
    degrees: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        withTransform({
            // Move to center only, rotation will be handled differently
            translate(size.width / 2f, size.height / 2f)
        }) {
            val radius = minOf(size.width, size.height) / 2f * 0.8f
            val innerRadius = radius * 0.6f  // Adjusted inner radius

            // Apply rotation around the center point for the path
            val rotatedPath = Path().apply {
                // Outer arc with gap at the top
                arcTo(
                    rect = Rect(
                        left = -radius,
                        top = -radius,
                        right = radius,
                        bottom = radius
                    ),
                    startAngleDegrees = -60f + degrees,  // Adjust starting point and add rotation
                    sweepAngleDegrees = 300f,
                    forceMoveTo = true
                )

                // Inner arc, matching the outer arc's gap
                arcTo(
                    rect = Rect(
                        left = -innerRadius,
                        top = -innerRadius,
                        right = innerRadius,
                        bottom = innerRadius
                    ),
                    startAngleDegrees = 240f + degrees,
                    sweepAngleDegrees = -300f,
                    forceMoveTo = false
                )

                close()
            }

            drawPath(
                path = rotatedPath,
                color = color,
                style = Fill
            )
        }
    }
}


@Composable
fun AnimatedOpenCvLogo(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF4184F3),
    lightColor: Color = Color(0xFFAACBFB),
) {
    // Animation state
    val transition = rememberInfiniteTransition(label = "logo_rotation")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 1000 using LinearOutSlowInEasing
                360f at 1000 using LinearOutSlowInEasing
                360f at 2000 using LinearOutSlowInEasing
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    // Color transition
    val colorTransition by transition.animateColor(
        initialValue = primaryColor,
        targetValue = lightColor,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_transition"
    )

    // Scale animation
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    BoxWithConstraints(
        modifier = modifier

    ) {
        // Calculate sizes based on the minimum dimension to maintain aspect ratio
        val minDimension = minOf(constraints.maxWidth, constraints.maxHeight)
        val shapeSize = (minDimension * 0.2f).dp  // Shape takes 40% of the minimum dimension
        val spacing = (shapeSize * 0.4f)    // Spacing takes 20% of the minimum dimension
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    rotationZ = rotation
                },
        ) {
            // First shape
            Box(
                modifier = Modifier
                    .offset(y = spacing, x = spacing)
                    .graphicsLayer {
                        rotationZ = -rotation
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                OpenCvShape(
                    degrees = 0f,
                    color = colorTransition,
                    modifier = Modifier.size(shapeSize)
                )
            }

            // Second shape
            Box(
                modifier = Modifier
                    .offset(y = -spacing)
                    .graphicsLayer {
                        rotationZ = -rotation
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                OpenCvShape(
                    degrees = 180f,
                    color = colorTransition,
                    modifier = Modifier.size(shapeSize)
                )
            }

            // Third shape
            Box(
                modifier = Modifier
                    .offset(x = -spacing, y = spacing)
                    .graphicsLayer {
                        rotationZ = -rotation
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                OpenCvShape(
                    degrees = 60f,
                    color = colorTransition,
                    modifier = Modifier.size(shapeSize)
                )
            }
        }

    }
}

@Preview
@Composable
fun OpenCVPreview() {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        AnimatedOpenCvLogo(modifier = Modifier.size(24.dp))
    }
}
