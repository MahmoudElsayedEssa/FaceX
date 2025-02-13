package com.example.facex.ui.screens.performancemetrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facex.domain.performancetracking.MetricData
import com.example.facex.domain.performancetracking.PerformanceTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PerformanceMetricsState(
    val metrics: Map<String, MetricData> = emptyMap(),
)

@OptIn(FlowPreview::class)
@HiltViewModel
class PerformanceMetricsViewModel @Inject constructor(
    val performanceTracker: PerformanceTracker
) : ViewModel() {

    val state = performanceTracker.metricsFlow
        .map { metrics ->
            val metricsMap = metrics.metricDataMap()
            PerformanceMetricsState(
                metrics = metricsMap,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PerformanceMetricsState()
        )
}
