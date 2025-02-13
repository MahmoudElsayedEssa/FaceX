package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

//@Composable
//fun SparkleEffect(
//    modifier: Modifier = Modifier,
//    size: Dp = 60.dp,
//    color: Color = MaterialTheme.colorScheme.primary
//) {
//    var sparkleAngle by remember { mutableFloatStateOf(0f) }
//
//    LaunchedEffect(Unit) {
//        coroutineScope {
//            launch {
//                while (true) {
//                    animate(
//                        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
//                            animation = tween(
//                                3000, easing = LinearEasing
//                            ), repeatMode = RepeatMode.Restart
//                        )
//                    ) { value, _ -> sparkleAngle = value }
//                }
//            }
//        }
//    }
//
//    Canvas(modifier = modifier
//        .size(60.dp)
//        .graphicsLayer {
//            rotationZ = -sparkleAngle
//        }) {
//        val radius = size.toPx() / 4
//        for (i in 0..11) {
//            val angle = (i * 30f) * (PI / 180f).toFloat()
//            val sparkleScale = (1f + sin(sparkleAngle * (PI / 180f).toFloat() + i)) / 2f
//            val startX = center.x + cos(angle) * (radius * sparkleScale)
//            val startY = center.y + sin(angle) * (radius * sparkleScale)
//            val endX = center.x + cos(angle) * (radius * 0.5f * sparkleScale)
//            val endY = center.y + sin(angle) * (radius * 0.5f * sparkleScale)
//
//            drawLine(
//                color = color.copy(alpha = 0.7f * sparkleScale),
//                start = Offset(startX, startY),
//                end = Offset(endX, endY),
//                strokeWidth = 2f * sparkleScale
//            )
//        }
//    }
//}

@Composable
fun SparkleEffect(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition()
    val sparkleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val sparkleOffsets by remember {
        derivedStateOf {
            (0..11).map { i ->
                val angle = (i * 30f) * (PI / 180f).toFloat() // Convert degrees to radians
                angle
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val maxSize = minOf(maxWidth, maxHeight)
        val radius = maxSize / 2f

        Canvas(
            modifier = Modifier
                .size(maxSize)
                .graphicsLayer {
                    rotationZ = -sparkleAngle
                }
        ) {
            val sparkleScaleFactor = sparkleAngle * (PI / 180f).toFloat()

            sparkleOffsets.forEachIndexed { i, angle ->
                val sparkleScale = (1f + sin(sparkleScaleFactor + i)) / 2f

                val startX = center.x + cos(angle) * (radius.toPx() * sparkleScale)
                val startY = center.y + sin(angle) * (radius.toPx() * sparkleScale)
                val endX = center.x + cos(angle) * (radius.toPx() * 0.5f * sparkleScale)
                val endY = center.y + sin(angle) * (radius.toPx() * 0.5f * sparkleScale)

                drawLine(
                    color = color.copy(alpha = 0.7f * sparkleScale),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2f * sparkleScale
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SparkleEffectFlexiblePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        SparkleEffect(
            modifier = Modifier.size(150.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
