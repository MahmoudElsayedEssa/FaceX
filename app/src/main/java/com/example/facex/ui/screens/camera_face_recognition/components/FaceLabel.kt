package com.example.facex.ui.screens.camera_face_recognition.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn

@Composable
fun FaceLabel(
    text: String, faceWidth: Float, faceHeight: Float, modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scaleBase = minOf(faceWidth, faceHeight)

    if (scaleBase < 200f) return

    // Dynamically calculate size-related properties
    val dynamicTextSize = remember(scaleBase) { (scaleBase * 0.12f).fastCoerceIn(8f, 18f) }
    val minimumMargin = with(density) { 16.dp.toPx() }
    val cornerMargin = faceWidth * 0.1f
    val availableWidth = faceWidth - (minimumMargin + cornerMargin * 2)

    val dynamicPaddingHorizontal = remember(dynamicTextSize) {
        (dynamicTextSize * 0.35f).fastCoerceIn(4f, 12f)
    }
    val dynamicPaddingVertical = remember(dynamicTextSize) {
        (dynamicTextSize * 0.15f).fastCoerceIn(2f, 6f)
    }
    val dynamicCornerRadius = remember(dynamicTextSize) {
        (dynamicTextSize * 0.25f).fastCoerceIn(4f, 8f)
    }

    Box(
        modifier = modifier
            .padding(
                start = cornerMargin.dp, end = cornerMargin.dp
            )
            .widthIn(min = dynamicTextSize.dp * 3, max = availableWidth.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(dynamicCornerRadius.dp),
            color = Color(0xFF00C853).copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = dynamicTextSize.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = dynamicPaddingHorizontal.dp, vertical = dynamicPaddingVertical.dp
                )
            )
        }
    }
}

