package com.example.facex.ui.components.icons.animated

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//@Composable
//fun RotatedIcon(
//    modifier: Modifier = Modifier,
//    icon: ImageVector,
//    contentDescription: String,
//    rotateBy: Float = 180f,
//    rotationToggle: Int,
//    delay: Int = 0,
//    tint: Color = Color.White
//) {
//    var currentRotation by remember { mutableFloatStateOf(0f) }
//    val rotation = remember { Animatable(0f) }
//    val scale = remember { Animatable(1f) }
//
//    LaunchedEffect(Unit) {
//
//        delay(delay.toLong())
//
//        launch {
//            scale.animateTo(
//                targetValue = 0.8f,
//                animationSpec = tween(200, easing = FastOutSlowInEasing)
//            )
//
//            rotation.animateTo(
//                targetValue = 720f,
//                animationSpec = tween(
//                    durationMillis = 600,
//                    easing = FastOutSlowInEasing
//                )
//            )
//
//            scale.animateTo(
//                targetValue = 1f,
//                animationSpec = spring(
//                    dampingRatio = 0.5f,
//                    stiffness = Spring.StiffnessMedium
//                )
//            )
//        }
//        currentRotation = 720f
//    }
//
//    LaunchedEffect(rotationToggle) {
//        currentRotation += rotateBy
//
//        coroutineScope {
//            launch {
//                // Quick scale down
//                scale.animateTo(
//                    targetValue = 0.9f,
//                    animationSpec = tween(150, easing = FastOutSlowInEasing)
//                )
//                // Bounce back
//                scale.animateTo(
//                    targetValue = 1f,
//                    animationSpec = spring(
//                        dampingRatio = 0.5f,
//                        stiffness = Spring.StiffnessMedium
//                    )
//                )
//            }
//
//            launch {
//                // Smooth rotation with slight overshoot
//                rotation.animateTo(
//                    targetValue = currentRotation + 15f, // Overshoot
//                    animationSpec = tween(
//                        durationMillis = 300,
//                        easing = FastOutSlowInEasing
//                    )
//                )
//                // Settle back to target
//                rotation.animateTo(
//                    targetValue = currentRotation,
//                    animationSpec = spring(
//                        dampingRatio = 0.6f,
//                        stiffness = Spring.StiffnessMedium
//                    )
//                )
//            }
//        }
//    }
//
//    Icon(
//        imageVector = icon,
//        tint = tint,
//        contentDescription = contentDescription,
//        modifier = modifier
//            .graphicsLayer {
//                rotationZ = rotation.value
//                scaleX = scale.value
//                scaleY = scale.value
//            }
//    )
//}

@Composable
fun RotatedIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    baseRotationDegrees: Float = 180f,
    isClockwise: Boolean,
    rotationTrigger: Int,
    delayMs: Int = 0,
    tint: Color = Color.White,
    initialRotationEnabled: Boolean = true,
    initialRotationDegrees: Float = 720f,
    initialScale: Float = 1f,
    animationDuration: Int = 600,
    scaleDownFactor: Float = 0.8f,
    scaleBounceFactor: Float = 0.9f
) {
    var currentRotation by remember { mutableFloatStateOf(0f) }
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(initialScale) }

    // Perform initial rotation if enabled
    if (initialRotationEnabled) {
        LaunchedEffect(Unit) {
            delay(delayMs.toLong())
            launch {
                scale.animateTo(
                    targetValue = scaleDownFactor,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
                rotation.animateTo(
                    targetValue = initialRotationDegrees,
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                )
                scale.animateTo(
                    targetValue = initialScale,
                    animationSpec = spring(
                        dampingRatio = 0.5f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            currentRotation = initialRotationDegrees
        }
    }

    // React to rotationTrigger changes and rotate in specified direction
    LaunchedEffect(rotationTrigger) {
        // Adjust rotation based on direction
        val rotationDelta = if (isClockwise) baseRotationDegrees else -baseRotationDegrees
        currentRotation += rotationDelta

        coroutineScope {
            launch {
                // Quick scale down and bounce back
                scale.animateTo(
                    targetValue = scaleBounceFactor,
                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                )
                scale.animateTo(
                    targetValue = initialScale,
                    animationSpec = spring(
                        dampingRatio = 0.5f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                // Smooth rotation with slight overshoot
                rotation.animateTo(
                    targetValue = currentRotation + (if (isClockwise) 15f else -15f), // Overshoot
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
                rotation.animateTo(
                    targetValue = currentRotation,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    Icon(
        imageVector = icon,
        tint = tint,
        contentDescription = contentDescription,
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            }
    )
}


