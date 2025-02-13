package com.example.facex.ui.screens.camera_face_recognition.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.facex.domain.entities.Timestamp
import com.example.facex.domain.entities.Value


@Composable
fun SimpleLineChart(
    values: Map<Timestamp, Value>,
    color: Color,
    modifier: Modifier = Modifier
) {

    val animatedValues by remember(values) {
        derivedStateOf { values.entries.sortedBy { it.key }.takeLast(20) }
    }

    val maxValue = 120

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw grid lines
            val gridColor = color.copy(alpha = 0.2f)
            repeat(4) { i ->
                val y = size.height * i / 3
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw animated graph line
            if (animatedValues.isNotEmpty()) {
                val xStep = size.width / (animatedValues.size - 1)
                val path = Path().apply {
                    animatedValues.forEachIndexed { index, (_, usage  ) ->
                        val x = index * xStep
                        val y = size.height * (1 - (usage.toFloat() / maxValue))

                        if (index == 0) moveTo(x, y)
                        else lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
