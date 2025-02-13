package com.example.facex.ui.components.icons.animated

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedMediaPipe(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 24.dp
) {
    val transition = rememberInfiniteTransition(label = "MediaPipe Icon Animation")

    // First column divider animation (gap expands outward)
    val firstDividerProgress by transition.animateFloat(
        initialValue = 0f, // Start with middle gap
        targetValue = 1f,  // Expand gap outward
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "First Divider Animation"
    )

    // Second column divider animation
    val thirdDividerProgress by transition.animateFloat(
        initialValue = 0.1f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "Second Divider Animation"
    )

    // Third column divider animation
    val secondDividerProgress by transition.animateFloat(
        initialValue = 0.9f, targetValue = 0f, animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "Third Divider Animation"
    )

    // Last column animation
    val lastColumnProgress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "Last Column Animation"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .padding(2.dp)
    ) {
        val width = size.toPx()
        val columnWidth = width / 7f
        val spacing = columnWidth * 1.2f
        val cornerRadius = columnWidth / 2f

        val startY = columnWidth / 2f
        val totalHeight = width - columnWidth
        val middleY = startY + (totalHeight / 2)

        // First column with outward expanding gap
        val baseGap = spacing // Initial gap size
        val maxExtraGap = totalHeight * 0.5f // Maximum additional gap size
        val extraGap = maxExtraGap * firstDividerProgress
        val totalGap = baseGap + extraGap

        // Calculate positions for first column parts
        val topPartHeight = (totalHeight - totalGap) / 2
        val bottomPartStart = middleY + totalGap / 2

        // Draw first column top part
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, startY),
            size = Size(columnWidth, topPartHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
        // Draw first column bottom part
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, bottomPartStart),
            size = Size(columnWidth, topPartHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        // Second column
        val x2 = columnWidth * 4
        val thirdDividerY = startY + (totalHeight * thirdDividerProgress)
        drawRoundRect(
            color = color,
            topLeft = Offset(x2, startY),
            size = Size(columnWidth, thirdDividerY - startY),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
        if (thirdDividerY + columnWidth < startY + totalHeight) {
            drawRoundRect(
                color = color,
                topLeft = Offset(x2, thirdDividerY + spacing),
                size = Size(columnWidth, totalHeight - (thirdDividerY - startY) - spacing),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }

        // Third column
        val x3 = columnWidth * 2
        val secondDividerY = startY + (totalHeight * secondDividerProgress)
        drawRoundRect(
            color = color,
            topLeft = Offset(x3, startY),
            size = Size(columnWidth, secondDividerY - startY),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
        if (secondDividerY + columnWidth < startY + totalHeight) {
            drawRoundRect(
                color = color,
                topLeft = Offset(x3, secondDividerY + spacing),
                size = Size(columnWidth, totalHeight - (secondDividerY - startY) - spacing),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }

        // Last column (shrinks from edges)
        val x4 = columnWidth * 6
        val currentHeight = totalHeight * (1f - lastColumnProgress)
        val lastColumnY = middleY - (currentHeight / 2)

        drawRoundRect(
            color = color,
            topLeft = Offset(x4, lastColumnY),
            size = Size(columnWidth, currentHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
    }
}
