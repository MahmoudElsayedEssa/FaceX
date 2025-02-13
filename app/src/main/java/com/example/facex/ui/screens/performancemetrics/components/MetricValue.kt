package com.example.facex.ui.screens.performancemetrics.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.facex.ui.utils.formatDetailed
import kotlin.time.Duration.Companion.microseconds


@Composable
 fun MetricValue(
    label: String, value: Float, color: Color, modifier: Modifier = Modifier
) {
    val animatedMillis by animateFloatAsState(
        targetValue = value, animationSpec = tween(durationMillis = 500), label = ""
    )

    val animatedDuration = animatedMillis.toLong().microseconds

    val formattedDuration = animatedDuration.formatDetailed()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )



        Box(
            modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center
        ) {

            Text(
                text = formattedDuration,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}