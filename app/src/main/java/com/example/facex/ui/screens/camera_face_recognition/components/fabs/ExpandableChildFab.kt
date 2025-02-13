package com.example.facex.ui.screens.camera_face_recognition.components.fabs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExpandableChildFab(
    index: Int,
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    customFab: @Composable () -> Unit = {},
) {
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(AnimationConstants.COLLAPSED_SCALE) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            launch {
                // Reset initial values
                scale.snapTo(AnimationConstants.COLLAPSED_SCALE)
                alpha.snapTo(0f)
                offsetY.snapTo(0f)

                // Calculate target offset with staggered delay based on index
                val targetOffset = -(index + 1) * 65f
                val staggerDelay = index * AnimationConstants.CHILD_STAGGER_DELAY

                delay(staggerDelay.toLong())

                // Coordinated expansion animations
                launch {
                    // Overshoot and settle animation
                    offsetY.animateTo(
                        targetValue = targetOffset * AnimationConstants.OVERSHOOT_FACTOR,
                        animationSpec = tween(
                            AnimationConstants.EXPAND_ANIMATION_DURATION,
                            easing = FastOutSlowInEasing
                        )
                    )
                    offsetY.animateTo(
                        targetValue = targetOffset,
                        animationSpec = AnimationConstants.EXPAND_SPRING_SPEC
                    )
                }

                launch {
                    scale.animateTo(
                        targetValue = AnimationConstants.EXPANDED_SCALE,
                        animationSpec = AnimationConstants.EXPAND_SPRING_SPEC
                    )
                }

                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(AnimationConstants.ALPHA_ANIMATION_DURATION)
                    )
                }
            }
        } else {
            // Coordinated collapse animations
            launch {
                launch {
                    scale.animateTo(
                        targetValue = AnimationConstants.COLLAPSED_SCALE,
                        animationSpec = tween(AnimationConstants.COLLAPSE_ANIMATION_DURATION)
                    )
                }

                launch {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = AnimationConstants.COLLAPSE_SPRING_SPEC
                    )
                }

                launch {
                    delay(AnimationConstants.CHILD_ANIMATION_DELAY.toLong())
                    alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(AnimationConstants.ALPHA_ANIMATION_DURATION)
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .scale(scale.value)
            .graphicsLayer(alpha = alpha.value)
    ) {
        customFab()
    }
}
