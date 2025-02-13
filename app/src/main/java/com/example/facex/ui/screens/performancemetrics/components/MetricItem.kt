package com.example.facex.ui.screens.performancemetrics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.facex.domain.performancetracking.MetricData
import com.example.facex.ui.utils.splitToWords


@Composable
fun MetricItem(
    key: String,
    data: MetricData,
    modifier: Modifier = Modifier
) {
    val valueColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val label = remember(key) { key.splitToWords() }

    Surface(
        modifier = modifier.height(IntrinsicSize.Min),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricValue(
                        label = "LAST",
                        value = data.lastValue.inWholeMicroseconds.toFloat(),
                        color = valueColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                )

                Box(modifier = Modifier.weight(1f)) {
                    MetricValue(
                        label = "AVG",
                        value = data.average.inWholeMicroseconds.toFloat(),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
