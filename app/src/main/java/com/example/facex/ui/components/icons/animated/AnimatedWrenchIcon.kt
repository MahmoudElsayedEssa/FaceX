package com.example.facex.ui.components.icons.animated

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.facex.ui.screens.camera_face_recognition.components.fabs.AnimationConstants
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedWrenchIcon(
    contentDescription: String,
    modifier: Modifier = Modifier,
    delay: Int = AnimationConstants.ICON_BASE_DELAY,
    tint: Color = Color.White,
    jumpingToggle: Int
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val translation = remember { Animatable(0f) }

    LaunchedEffect(jumpingToggle) {
        // Wait for FAB to settle in position
        delay(delay.toLong() + AnimationConstants.POSITION_SETTLE_DELAY)

        coroutineScope {
            // Initial movement
            launch {
                translation.animateTo(
                    targetValue = 15f,
                    animationSpec = tween(
                        durationMillis = AnimationConstants.ICON_ANIMATION_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                )
            }

            launch {
                scale.animateTo(
                    targetValue = 0.8f,
                    animationSpec = tween(
                        durationMillis = AnimationConstants.ICON_ANIMATION_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                )
            }

            // Double rotation animation
            repeat(2) {
                rotation.animateTo(
                    targetValue = -45f,
                    animationSpec = tween(
                        durationMillis = AnimationConstants.ICON_ANIMATION_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                )
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = AnimationConstants.ICON_ANIMATION_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                )
            }

            // Return to original position
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = AnimationConstants.ICON_SPRING_SPEC
                )
            }
            launch {
                translation.animateTo(
                    targetValue = 0f,
                    animationSpec = AnimationConstants.ICON_SPRING_SPEC
                )
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = translation.value
                    translationY = translation.value
                    rotationZ = rotation.value
                    transformOrigin = TransformOrigin(0.2f, 0.2f)
                }
        )
    }
}





