package com.example.facex.ui.components.icons.animated

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.facex.ui.screens.camera_face_recognition.components.fabs.AnimationConstants
import com.example.facex.ui.screens.camera_face_recognition.components.fabs.AnimationConstants.ICON_BASE_DELAY
import com.example.facex.ui.screens.camera_face_recognition.components.fabs.AnimationConstants.ICON_INITIAL_SCALE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun JumpingIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String,
    triggerAnimation: Int,
    delay: Int = ICON_BASE_DELAY,
    tint: Color = Color.White,
    onAnimationComplete: () -> Unit = {},
) {
    val iconScale = remember { Animatable(ICON_INITIAL_SCALE) }
    val iconOffsetY = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = triggerAnimation) {
        animateJumpingIcon(
            scope, iconScale, iconOffsetY, iconAlpha, delay, onAnimationComplete
        )
    }

    Icon(imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.graphicsLayer {
            scaleX = iconScale.value
            scaleY = iconScale.value
            translationY = iconOffsetY.value
            alpha = iconAlpha.value
        })
}


fun animateJumpingIcon(
    scope: CoroutineScope,
    iconScale: Animatable<Float, AnimationVector1D>,
    iconOffsetY: Animatable<Float, AnimationVector1D>,
    iconAlpha: Animatable<Float, AnimationVector1D>,
    delay: Int,
    onAnimationComplete: () -> Unit
) {

    scope.launch {
        this.coroutineContext.cancelChildren()
        try {
            // Reset values
            awaitAll(async { iconScale.snapTo(ICON_INITIAL_SCALE) },
                async { iconOffsetY.snapTo(0f) },
                async { iconAlpha.snapTo(0f) })

            delay(delay.toLong() + AnimationConstants.POSITION_SETTLE_DELAY)

            awaitAll(async {
                iconAlpha.animateTo(
                    1f, tween(AnimationConstants.ALPHA_ANIMATION_DURATION)
                )
            }, async {
                iconScale.animateTo(
                    AnimationConstants.ICON_MID_SCALE,
                    tween(AnimationConstants.ICON_SCALE_DURATION, easing = LinearEasing)
                )

                iconScale.animateTo(
                    AnimationConstants.ICON_FINAL_SCALE, AnimationConstants.ICON_SPRING_SPEC
                )

                onAnimationComplete()
            }, async {
                // Jump animation
                iconOffsetY.animateTo(
                    -AnimationConstants.ICON_JUMP_HEIGHT, tween(
                        AnimationConstants.ICON_ANIMATION_DURATION, easing = LinearEasing
                    )
                )

                iconOffsetY.animateTo(
                    0f, AnimationConstants.ICON_SPRING_SPEC
                )
            })

        } catch (e: CancellationException) {
            iconScale.snapTo(ICON_INITIAL_SCALE)
            iconOffsetY.snapTo(0f)
            iconAlpha.snapTo(0f)
        }
    }
}


@Preview
@Composable
fun JumpingIconPreview() {
    var triggerAnimation by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        // Trigger animation every 2 seconds
        while (true) {
            delay(2000)
            triggerAnimation++
        }
    }
    Box(
        modifier = Modifier.size(100.dp),
    ) {
        JumpingIcon(icon = Icons.Filled.Favorite,
            tint = Color.Red,
            contentDescription = "Favorite Icon",
            triggerAnimation = triggerAnimation,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center),
            onAnimationComplete = {
                Toast.makeText(context, "Animation Complete", Toast.LENGTH_SHORT).show()
            })

    }
}