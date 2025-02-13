package com.example.facex.ui.screens.camera_face_recognition.components.bottomsheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facex.ui.screens.camera_face_r.PerformanceMetricsScreen
import com.example.facex.ui.screens.performancemetrics.components.CpuMonitorCard
import com.example.facex.ui.screens.performancemetrics.components.FpsCard
import com.example.facex.ui.screens.performancemetrics.PerformanceMetricsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceBottomSheet(
    averageFps: Int,
    sheetState: SheetState,
    onClearMetrics: () -> Unit,
    onDismissBottomSheet: () -> Unit
) {
    val viewModel: PerformanceMetricsViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    CustomBottomSheet(sheetState = sheetState, onDismiss = onDismissBottomSheet, header = {
        Text(
            text = "Performance Metrics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

    }, content = {
        PerformanceMetricsScreen(uiState)
    }, footer = {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {

            }
            CpuMonitorCard(modifier = Modifier.fillMaxWidth())
            FpsCard(averageFps)
        }
    })
}
