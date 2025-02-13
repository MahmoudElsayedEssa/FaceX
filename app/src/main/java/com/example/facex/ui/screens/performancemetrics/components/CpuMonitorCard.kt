package com.example.facex.ui.screens.performancemetrics.components


import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facex.ui.components.linechart.LineChart
import com.example.facex.ui.components.linechart.models.ChartPoint
import com.example.facex.ui.helpers.CpuCollector
import com.example.facex.ui.helpers.PreciseCpuMetrics
import com.example.facex.ui.helpers.ProcessStats
import com.example.facex.ui.screens.camera_face_recognition.components.SimpleLineChart
import java.time.Instant
import kotlin.time.Duration


@Composable
fun CpuMonitorCard(modifier: Modifier) {
    val cpuUsage by CpuCollector.metricsFlow.collectAsStateWithLifecycle(
        initialValue = PreciseCpuMetrics(
            usage = 0.0,
            timestamp = Instant.now(),
            duration = Duration.ZERO,
            processCpuTime = Duration.ZERO,
            systemCpuTime = Duration.ZERO,
            processStats = ProcessStats(0, 0, 0, 0)
        )
    )

    val history by CpuCollector.historyFlow.collectAsStateWithLifecycle()
    var showDetailedGraph by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(onClick = { showDetailedGraph = true })
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Current CPU Usage",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedCpuValue(
                value = cpuUsage.usage,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(45.dp)
        ) {
            SimpleLineChart(
                values = history.data, color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showDetailedGraph) {
        DetailedCpuGraph(history = history, onDismiss = { showDetailedGraph = false })
    }
}

@Composable
private fun DetailedCpuGraph(
    history: CpuCollector.CpuHistory, onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(400.dp),
            shape = MaterialTheme.shapes.large,
            color = CardDefaults.elevatedCardColors().containerColor,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CPU Usage History", style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "Close"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))


                LineChart(
                    data = history.data, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
private fun AnimatedCpuValue(
    value: Double, style: TextStyle, modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(), animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "CPU Usage Animation"
    )

    Text(
        text = "%.1f%%".format(animatedValue),
        style = style,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}
