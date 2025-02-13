package com.example.facex.ui.screens.camera_face_r

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.facex.ui.screens.performancemetrics.PerformanceMetricsState
import com.example.facex.ui.screens.performancemetrics.components.MetricItem


@Composable
fun PerformanceMetricsScreen(state: PerformanceMetricsState) {

    Box(modifier = Modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = state.metrics.toList(), key = { (key, _) -> key }) { (key, data) ->
                key(key) {
                    MetricItem(
                        key = key, data = data, modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}


