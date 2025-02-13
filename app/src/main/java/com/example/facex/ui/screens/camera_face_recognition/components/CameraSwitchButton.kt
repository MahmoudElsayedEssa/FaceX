package com.example.facex.ui.screens.camera_face_recognition.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.tan


@Composable
fun CameraSwitchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animation states
    var isAnimating by remember { mutableStateOf(true) }
    val rotation = remember { Animatable(0f) }

    // Shine effect animation
    val shineOffset = remember { Animatable(-100f) }
    val shineAlpha = remember { Animatable(0f) }

    // Handle animations
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            // Rotate the icon
            launch {
                rotation.animateTo(
                    targetValue = rotation.value + 180f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }

            // Shine effect
            launch {
                shineAlpha.animateTo(0.7f, tween(100))
                shineOffset.animateTo(100f, tween(300))
                shineAlpha.animateTo(0f, tween(100))
                shineOffset.snapTo(-100f)
                isAnimating = false
            }
        }
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable {
                if (!isAnimating) {
                    isAnimating = true
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Main icon
        Icon(
            imageVector = Icons.Default.Face,
            contentDescription = "Switch camera",
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationY = rotation.value
                },
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // Angled shine effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    // Calculate diagonal line parameters
                    val diagonal = sqrt(size.width * size.width + size.height * size.height)
                    val angleRad = 45f * PI.toFloat() / 180f // 45-degree angle
                    val shineWidth = diagonal / 3

                    // Calculate shine position
                    val x = shineOffset.value
                    val y = x * tan(angleRad)

                    // Create rotated gradient
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = shineAlpha.value),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset(x - shineWidth/2, y - shineWidth/2),
                            end = Offset(x + shineWidth/2, y + shineWidth/2)
                        ),
                        blendMode = BlendMode.Screen,
                        size = Size(diagonal, diagonal),
                        topLeft = Offset(
                            -diagonal/2 + size.width/2,
                            -diagonal/2 + size.height/2
                        )
                    )
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraSwitchButtonPreview() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CameraSwitchButton(onClick = {})
    }
}