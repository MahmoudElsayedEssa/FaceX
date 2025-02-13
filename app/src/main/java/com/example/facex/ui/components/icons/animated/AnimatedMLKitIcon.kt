package com.example.facex.ui.components.icons.animated

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun AnimatedMLKitLogo(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF4184F3),
    lightColor: Color = Color(0xFFAACBFB)
) {
    val transition = rememberInfiniteTransition(label = "ML Writing Animation")

    val pathProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Path Progress"
    )

    BoxWithConstraints(
        modifier = modifier
    ) {
        val boxWidth = constraints.maxWidth
        val boxHeight = constraints.maxHeight
        val aspectRatio = 1.5f

        Canvas(
            modifier = modifier
                .aspectRatio(aspectRatio)
        ) {

            val scale = minOf(boxWidth, boxHeight) * 0.8f

            translate(
                left = (boxWidth - scale) / 2,
                top = (boxHeight - scale) / 2
            ) {
                val strokeWidth = scale * 0.12f
                val strokeStyle = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )

                // M Path with adjusted middle V
                val mPath = Path().apply {
                    // First diagonal of M
                    moveTo(scale * 0.01f, scale * 0.8f)

                    lineTo(scale * 0.40f, scale * 0.01f)

                    lineTo(scale * 0.40f, scale * 0.8f)

                    lineTo(scale * 0.80f, scale * 0.01f)
                }

                // L Path
                val lPath = Path().apply {
                    moveTo(scale * 0.8f, scale * 0.01f)
                    lineTo(scale * 0.8f, scale * 0.8f)
                    lineTo(scale * 1.05f, scale * 0.8f)
                }

                // Draw background paths
                drawPath(
                    path = mPath,
                    color = lightColor,
                    style = strokeStyle
                )

                drawPath(
                    path = lPath,
                    color = lightColor,
                    style = strokeStyle
                )

                // Animate paths
                val mProgress = (pathProgress * 1.67f).coerceAtMost(1f)
//             val lProgress = ((pathProgress - 0.3f) * 1.5f).coerceIn(0f, 1f)

                // L starts only after N is complete
                val lProgress = ((pathProgress - 0.6f) * 2.5f).coerceIn(0f, 1f)
                // Draw M animation
                val animatedMPath = Path()
                PathMeasure().apply {
                    setPath(mPath, false)
                    getSegment(
                        startDistance = 0f,
                        stopDistance = length * mProgress,
                        destination = animatedMPath,
                        startWithMoveTo = true
                    )
                }

                // Draw L animation
                val animatedLPath = Path()
                PathMeasure().apply {
                    setPath(lPath, false)
                    getSegment(
                        startDistance = 0f,
                        stopDistance = length * lProgress,
                        destination = animatedLPath,
                        startWithMoveTo = true
                    )
                }

                // Draw animated paths
                drawPath(
                    path = animatedMPath,
                    color = primaryColor,
                    style = strokeStyle
                )

                drawPath(
                    path = animatedLPath,
                    color = primaryColor,
                    style = strokeStyle
                )
            }
        }

    }
}

@Preview
@Composable
fun PreviewAnimatedMLKit() {
    Box(
        modifier = Modifier
            .size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Log.d("TAG", "PreviewAnimatedMLKit: ")
        AnimatedMLKitLogo(
            modifier = Modifier,
            primaryColor = Color(0xFF4184F3)
        )
    }
}
