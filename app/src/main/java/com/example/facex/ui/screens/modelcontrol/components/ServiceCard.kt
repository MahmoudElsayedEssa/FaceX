package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.facex.domain.entities.ModelAcceleration
import com.example.facex.domain.entities.ModelOption
import com.example.facex.domain.entities.ProcessorType
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.ui.components.ItemPhase
import com.example.facex.ui.components.icons.animated.AnimatedMLKitLogo
import com.example.facex.ui.components.icons.animated.AnimatedMediaPipe
import com.example.facex.ui.components.icons.animated.AnimatedOpenCvLogo
import com.example.facex.ui.components.icons.animated.AnimatedTensorFlowLogo


val reusableItemModifier = Modifier.size(24.dp)


@Composable
fun ServiceCard(
    option: ServiceOption,
    modifier: Modifier = Modifier,
    onSaveChanges: (ModelOption, Float, ModelAcceleration) -> Unit,
    onChangeService: (ProcessorType) -> Unit,
    itemPhase: ItemPhase,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val serviceOption by remember(option) { mutableStateOf(option) }
    var isExpanded by remember { mutableStateOf(false) }

    AnimatedBorder(enabled = serviceOption.isCurrent) { borderModifier ->

        OutlinedCard(
            modifier = modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { isExpanded = !isExpanded }
                )
                .then(borderModifier)
                .animateContentSize(
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow
                    )
                ),

            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(3f)
                ) {
                    when (serviceOption.serviceType) {
                        is ProcessorType.Detection.MLKit ->
                            AnimatedMLKitLogo(reusableItemModifier)

                        is ProcessorType.Detection.MediaPipe ->
                            AnimatedMediaPipe(reusableItemModifier)

                        is ProcessorType.Detection.OpenCV ->
                            AnimatedOpenCvLogo(reusableItemModifier)

                        is ProcessorType.Recognition.LiteRT ->
                            AnimatedTensorFlowLogo(reusableItemModifier)

                        is ProcessorType.Recognition.OpenCV ->
                            AnimatedOpenCvLogo(reusableItemModifier)

                        else -> {}
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = serviceOption.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (serviceOption.isCurrent) CurrentLabel(itemPhase)
                        }

                        Text(
                            text = serviceOption.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (serviceOption.isCurrent) SparkleEffect(modifier = reusableItemModifier)
                    else RunButton {
                        onChangeService(serviceOption.serviceType)
                    }

                    ArrowIndicator(
                        { isExpanded = !isExpanded },
                        reusableItemModifier,
                        interactionSource = interactionSource,
                        isExpanded = isExpanded,
                    )
                }
            }

            ExpandedContent(
                models = serviceOption.models,
                isExpanded = isExpanded,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onSaveChanges = onSaveChanges,
            )
        }
    }
}
