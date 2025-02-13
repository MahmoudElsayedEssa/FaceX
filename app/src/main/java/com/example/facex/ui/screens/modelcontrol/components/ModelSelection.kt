package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.example.facex.domain.entities.ModelAcceleration
import com.example.facex.domain.entities.ModelOption

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelSelection(
    selectedModel: ModelOption?,
    models: List<ModelOption>,
    onModelSelected: (ModelOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Header Text
        Text(
            text = "Select Model",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            models.fastForEach { model ->
                AnimatedChip(
                    text = model.name,
                    isSelected = model == selectedModel,
                    onSelect = { onModelSelected(model) },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedChip(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier

) {
    val interactionSource = remember { MutableInteractionSource() }
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        label = "chip_elevation"
    )

    SuggestionChip(
        onClick = onSelect,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = animateColorAsState(
                targetValue = if (isSelected) colorScheme.primaryContainer
                else colorScheme.surfaceVariant,
                label = "chip_color"
            ).value
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            true,
            borderColor = animateColorAsState(
                targetValue = if (isSelected) colorScheme.primary
                else colorScheme.outline,
                label = "chip_border"
            ).value
        ),
        elevation = SuggestionChipDefaults.suggestionChipElevation(elevation),
        interactionSource = interactionSource,
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun ModelSelectionPreview() {
    ModelSelection(
        selectedModel = ModelOption(
            name = "Model A",
            id = 1,
            description = "TODO()",
            isCurrent = true,
            modelAcceleration = ModelAcceleration.GPU
        ),
        models = listOf(
            ModelOption(
                name = "Model B",
                id = 2,
                description = "TODO()",
                isCurrent = false,
                modelAcceleration = ModelAcceleration.GPU
            ),
            ModelOption(
                name = "Model C",
                id = 3,
                description = "TODO()",
                isCurrent = false,
                modelAcceleration = ModelAcceleration.GPU
            ),
            ModelOption(
                name = "Model C",
                id = 4,
                description = "TODO()",
                isCurrent = false,
                modelAcceleration = ModelAcceleration.GPU
            )
        ),
        onModelSelected = {}
    )
}

