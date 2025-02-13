package com.example.facex.ui.screens.camera_face_recognition.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.fastCoerceIn

@Composable
fun FaceBox(
    width: Float,
    height: Float,
    isRecognized: Boolean,
) {

    val boxSize = minOf(width, height)

    val dynamicCornerLength = (boxSize * 0.1f).fastCoerceIn(boxSize * 0.2f, boxSize * 0.2f)

    val dynamicStrokeWidth = (boxSize * 0.01f).fastCoerceIn(boxSize * 0.03f, boxSize * 0.1f)

    val dynamicCornerRadius = (dynamicCornerLength * 0.15f).fastCoerceIn(2f, boxSize * 0.02f)

    val animatedColor by animateColorAsState(
        targetValue = if (isRecognized) {
            Color(0xFF00C853)
        } else {
            Color(0xFFFF3D00)
        }, animationSpec = tween(300), label = "boxColor"
    )

    Canvas(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp)
    ) {

        // Adjust stroke width to prevent edge overlap
        val adjustedStroke = dynamicStrokeWidth / 2

        // Ensure minimum space between corners
        val minSpacing = dynamicStrokeWidth * 2
        val adjustedCornerPx = dynamicCornerLength.fastCoerceAtMost(
            minOf(size.width, size.height) / 3f - minSpacing // Allow up to 1/3 of box size
        )

        // Draw corners with adjusted dimensions
        drawCorner(
            start = Offset(adjustedStroke, adjustedCornerPx),
            end = Offset(adjustedStroke, adjustedStroke),
            corner = Offset(adjustedCornerPx, adjustedStroke),
            color = animatedColor,
            strokeWidth = dynamicStrokeWidth,
            cornerRadius = dynamicCornerRadius
        )

        drawCorner(
            start = Offset(size.width - adjustedCornerPx, adjustedStroke),
            end = Offset(size.width - adjustedStroke, adjustedStroke),
            corner = Offset(size.width - adjustedStroke, adjustedCornerPx),
            color = animatedColor,
            strokeWidth = dynamicStrokeWidth,
            cornerRadius = dynamicCornerRadius
        )

        drawCorner(
            start = Offset(adjustedStroke, size.height - adjustedCornerPx),
            end = Offset(adjustedStroke, size.height - adjustedStroke),
            corner = Offset(adjustedCornerPx, size.height - adjustedStroke),
            color = animatedColor,
            strokeWidth = dynamicStrokeWidth,
            cornerRadius = dynamicCornerRadius
        )

        drawCorner(
            start = Offset(size.width - adjustedCornerPx, size.height - adjustedStroke),
            end = Offset(size.width - adjustedStroke, size.height - adjustedStroke),
            corner = Offset(size.width - adjustedStroke, size.height - adjustedCornerPx),
            color = animatedColor,
            strokeWidth = dynamicStrokeWidth,
            cornerRadius = dynamicCornerRadius
        )
    }
}

private fun DrawScope.drawCorner(
    start: Offset,
    end: Offset,
    corner: Offset,
    color: Color,
    strokeWidth: Float,
    cornerRadius: Float
) {
    // Draw rounded corner using path
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(
            end.x, end.y, corner.x, corner.y
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth, cap = StrokeCap.Butt, join = StrokeJoin.Round,
        )
    )
}
