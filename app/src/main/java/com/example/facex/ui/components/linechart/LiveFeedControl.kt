package com.example.facex.ui.components.linechart

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.facex.ui.components.icons.filled.Pause
import com.example.facex.ui.components.linechart.models.ChartTheme
import kotlinx.coroutines.launch

@Composable
fun LiveFeedControl(
    isAutoScrolling: Boolean,
    onToggle: () -> Unit,
    theme: ChartTheme,
    modifier: Modifier = Modifier
) {
    // Create animated color for background and content
    val backgroundColor by animateColorAsState(
        targetValue = if (isAutoScrolling) {
            theme.lineColor.copy(alpha = 0.1f)
        } else {
            Color.Transparent
        }
    )

    val contentColor by animateColorAsState(
        targetValue = if (isAutoScrolling) {
            theme.lineColor
        } else {
            theme.labelColor
        }
    )

    // Create scale animation for the button press effect
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .clickable {
                onToggle()
                // Trigger scale animation on click
                scope.launch {
                    scale.animateTo(
                        targetValue = 0.95f,
                        animationSpec = tween(100)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
            .scale(scale.value),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated icon rotation
            val rotation by animateFloatAsState(
                targetValue = if (isAutoScrolling) 360f else 0f
            )

            Icon(
                imageVector = if (isAutoScrolling) {
                    Pause
                } else {
                    Icons.Rounded.PlayArrow
                },
                contentDescription = if (isAutoScrolling) {
                    "Stop live feed"
                } else {
                    "Start live feed"
                },
                modifier = Modifier
                    .size(if (isAutoScrolling) 12.dp else 18.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                tint = contentColor
            )

            // Animated text size and weight
            val textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isAutoScrolling) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )

            Text(
                text = if (isAutoScrolling) "Live" else "Live Feed",
                style = textStyle,
                color = contentColor
            )

            // Optional: Add pulsing dot animation when live
            if (isAutoScrolling) {
                val pulseSize by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .scale(pulseSize)
                        .clip(CircleShape)
                        .background(theme.lineColor)
                )
            }
        }
    }
}
