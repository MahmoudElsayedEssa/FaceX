package com.example.facex.ui.screens.performancemetrics.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facex.ui.components.linechart.LineChart
import com.example.facex.ui.helpers.FpsCollector
import com.example.facex.ui.screens.camera_face_recognition.components.SimpleLineChart


@Composable
fun FpsCard(fps: Int) {
    var showDetailedGraph by remember { mutableStateOf(false) }
    val history by FpsCollector.historyFlow.collectAsStateWithLifecycle()

    LaunchedEffect(fps) {
        FpsCollector.updateFps(fps)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { showDetailedGraph = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Average Output FPS",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedValue(
                value = fps,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(45.dp)
        ) {
            SimpleLineChart(
                values = history.data,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showDetailedGraph) {
        DetailedFpsGraph(
            history = history,
            onDismiss = { showDetailedGraph = false }
        )
    }
}

@Composable
private fun DetailedFpsGraph(
    history: FpsCollector.FpsHistory,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                        text = "FPS History",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                LineChart(
                    data = history.data,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AnimatedValue(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val animatedValue by animateIntAsState(
        targetValue = value.coerceAtMost(60), label = "value"
    )

    Text(
        text = animatedValue.toString(),
        style = style,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary
    )
}
