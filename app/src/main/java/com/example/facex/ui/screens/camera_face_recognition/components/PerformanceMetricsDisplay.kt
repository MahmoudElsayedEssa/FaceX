package com.example.facex.ui.screens.camera_face_recognition.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.facex.domain.entities.PerformanceTracker
import com.example.facex.ui.screens.camera_face_recognition.RecognitionViewModel
import com.example.facex.ui.utils.formatDuration


@Composable
fun PerformanceMetricsDisplay(viewModel: RecognitionViewModel = hiltViewModel()) {
    val performanceMetrics by viewModel.performanceMetrics.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Performance Metrics",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            if (performanceMetrics.getAllMetrics().isEmpty()) {
                item {
                    Text("No metrics available", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                val metricsList = performanceMetrics.getAllMetrics().toList()

                items(metricsList) { (key, data) ->
                    val lastValueFormatted = remember(data.lastValue) {
                        data.lastValue.formatDuration()
                    }
                    val averageFormatted = remember(data.average) {
                        data.average.formatDuration()
                    }

                    MetricItem(
                        key = key,
                        lastValueFormatted = lastValueFormatted,
                        averageFormatted = averageFormatted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.clearPerformanceMetrics() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Clear Metrics")
        }
    }
}


@Composable
fun MetricItem(
    key: PerformanceTracker.MetricKey,
    lastValueFormatted: String,
    averageFormatted: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = key.toString(), style = MaterialTheme.typography.titleMedium)
        Text(text = "Last: $lastValueFormatted", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Average: $averageFormatted", style = MaterialTheme.typography.bodyMedium)
    }
}
