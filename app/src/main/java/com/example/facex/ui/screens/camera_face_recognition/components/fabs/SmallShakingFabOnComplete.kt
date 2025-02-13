package com.example.facex.ui.screens.camera_face_recognition.components.fabs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun SmallShakingFabOnComplete(
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    content: @Composable (onComplete: () -> Unit) -> Unit,
) {
    var shouldShake by remember { mutableStateOf(false) }
    val yScale = remember { Animatable(0f) }
    val xScale = remember { Animatable(0f) }

    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            repeat(2) {
                yScale.animateTo(1.25f, tween(15, easing = FastOutLinearInEasing))
                xScale.animateTo(0.95f, tween(15, easing = FastOutLinearInEasing))

                yScale.animateTo(0.85f, tween(15, easing = FastOutLinearInEasing))

                yScale.animateTo(1f, tween(15, easing = FastOutLinearInEasing))
                xScale.animateTo(1f, tween(15, easing = FastOutLinearInEasing))
            }
            shouldShake = false
        }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleY = yScale.value
                scaleX = xScale.value
            }
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = backgroundColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(20.dp)
        ) {
            content { shouldShake = true }
        }
    }
}
