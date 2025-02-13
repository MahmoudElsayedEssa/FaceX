package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.facex.domain.entities.ModelAcceleration
import com.example.facex.domain.entities.ModelOption


@Composable
fun ExpandedContent(
    models: List<ModelOption>?,
    isExpanded: Boolean,
    onSaveChanges: (ModelOption, Float, ModelAcceleration) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    if (models == null) return

    var selectedModel by remember(models) { mutableStateOf(models.first { it.isCurrent }) }
    var threshold by remember(selectedModel) { mutableFloatStateOf(selectedModel.threshold) }
    var acceleration by remember(selectedModel) { mutableStateOf(selectedModel.modelAcceleration) }

    // Derived state for save button
    val hasChanges by remember(selectedModel, threshold, acceleration) {
        derivedStateOf {
            threshold != selectedModel.threshold ||
                    acceleration != selectedModel.modelAcceleration
        }
    }

    val saveButtonKey = remember(
        selectedModel, acceleration, threshold
    ) {
        // Combine all relevant state values into a single key
        "${selectedModel.id}-${acceleration}-${threshold}"
    }


    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
        ) + expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = density.run { IntSize(1, 1) }
            )
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
        ) + shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    ) {
        Column(
            modifier = modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Text(
                text = "Configure Model",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ModelSelection(
                selectedModel = selectedModel,
                onModelSelected = { model ->
                    selectedModel = model
                    threshold = model.threshold
                    acceleration = model.modelAcceleration
                },
                models = models,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ThresholdAdjuster(
                    threshold = threshold,
                    onThresholdChanged = { newThreshold ->
                        threshold = newThreshold
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AccelerationChipGroup(
                selectedAcceleration = acceleration,
                onAccelerationSelected = { newAcceleration ->
                    acceleration = newAcceleration
                },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = hasChanges,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                SaveButton(
                    onSave = {
                        onSaveChanges(selectedModel, threshold, acceleration)
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    key = saveButtonKey
                )
            }
        }
    }
}