package com.example.facex.ui.screens.modelcontrol.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp


@Composable
fun ArrowIndicator(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    isExpanded: Boolean,
) {

    val downArrowIcon = rememberVectorPainter(Icons.Default.KeyboardArrowDown)
    val upArrowIcon = rememberVectorPainter(Icons.Default.KeyboardArrowUp)
    val icon by remember(isExpanded) {
        derivedStateOf {
            if (!isExpanded) {
                downArrowIcon
            } else upArrowIcon
        }
    }
    val colorScheme = MaterialTheme.colorScheme
    val tintColor = remember(colorScheme) { colorScheme.primary }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                with(icon) {
                    draw(size, colorFilter = ColorFilter.tint(tintColor))
                }
            }
            .clickable(interactionSource = interactionSource,
                indication = null,
                onClick = onClick)
    )
}
