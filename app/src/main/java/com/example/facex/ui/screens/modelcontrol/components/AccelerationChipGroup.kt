package com.example.facex.ui.screens.modelcontrol.components

import com.example.facex.ui.components.icons.filled.NeuralNet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.facex.domain.entities.ModelAcceleration
import com.example.facex.ui.components.icons.filled.Cpu
import com.example.facex.ui.components.icons.filled.Gpu

@Composable
fun AccelerationChipGroup(
    selectedAcceleration: ModelAcceleration?,
    onAccelerationSelected: (ModelAcceleration) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier

    ) {
        Text(
            text = "Model Acceleration",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModelAcceleration.entries.forEach { acceleration ->
                val icon = when (acceleration) {
                    ModelAcceleration.CPU -> Cpu
                    ModelAcceleration.GPU -> Gpu
                    ModelAcceleration.NNAPI -> NeuralNet
                }

                FilterChip(
                    selected = acceleration == selectedAcceleration,
                    onClick = { onAccelerationSelected(acceleration) },
                    label = {
                        Text(
                            text = acceleration.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = acceleration.displayName,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colorScheme.surfaceVariant,
                        labelColor = colorScheme.onSurfaceVariant,
                        selectedContainerColor = colorScheme.primaryContainer,
                        selectedLabelColor = colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccelerationOptionsRowPreview() {
    AccelerationChipGroup(selectedAcceleration = ModelAcceleration.CPU, onAccelerationSelected = {})
}
