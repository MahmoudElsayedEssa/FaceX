package com.example.facex.ui.components.linechart

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facex.ui.components.linechart.models.ChartTheme

@Composable
fun SkipAnimationIndicator(
    duration: Long,
    isForward: Boolean,
    theme: ChartTheme,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    // Create sequential animation for the icons
    val infiniteTransition = rememberInfiniteTransition(label = "sequential")

    // Three separate animations for each icon, with carefully timed delays
    val animations = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 750,  // Total animation duration
                    easing = FastOutSlowInEasing,
                    delayMillis = index * 100  // Stagger between icons
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "sequential$index"
        )
    }

    Box(
        modifier = modifier
            .size(80.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            // Row of animated icons
            Row(
                horizontalArrangement = Arrangement.spacedBy((-6).dp),
                modifier = Modifier.height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val arrowIndices = if (isForward) (0..2) else (2 downTo 0)

                arrowIndices.forEach { index ->
                    val animationValue = animations[index].value

                    Icon(
                        // Using Forward arrow for consistency, rotating for backward direction
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(  // YouTube uses white icons
                            alpha = animationValue * 0.8f + 0.2f  // Base opacity of 0.2
                        ),
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = if (!isForward) 180f else 0f
                                // Subtle scale effect during animation
                                scaleX = 1f + (animationValue * 0.15f)
                                scaleY = 1f + (animationValue * 0.15f)
                            }
                    )
                }
            }

        }
    }
}

