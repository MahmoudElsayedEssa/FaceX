package com.example.facex.ui.screens.camera_face_recognition.components.fabs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.facex.ui.components.icons.animated.RotatedIcon
import kotlinx.coroutines.launch


@Composable
fun ExpandableActionFab(
    modifier: Modifier = Modifier,
    items: List<FabContentItem>,
    mainIcon: ImageVector = Icons.Default.Settings,
    mainFabBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    mainFabContentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val mainButtonScale = remember { Animatable(1f) }
    var toggleChildAnimation by remember { mutableIntStateOf(0) }

    var isClockwise by remember { mutableStateOf(true) }
    var rotationTrigger by remember { mutableIntStateOf(0) }

    Box(modifier = modifier) {
        // Child FABs
        items.forEachIndexed { index, item ->
            ExpandableChildFab(
                modifier = Modifier.offset(x = 4.dp),
                index = index,
                isExpanded = isExpanded,
                customFab = {
                    item.content(toggleChildAnimation)
                },
            )
        }

        val scope = rememberCoroutineScope()

        // Main FAB
        FloatingActionButton(
            shape = RoundedCornerShape(50.dp),
            onClick = {
                isExpanded = !isExpanded
                scope.launch {
                    launch {
                        if (isExpanded) {
                            // Expansion effect
                            mainButtonScale.animateTo(
                                targetValue = AnimationConstants.COLLAPSED_SCALE,
                                animationSpec = tween(
                                    AnimationConstants.SCALE_ANIMATION_DURATION,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            mainButtonScale.animateTo(
                                targetValue = AnimationConstants.EXPANDED_SCALE,
                                animationSpec = AnimationConstants.EXPAND_SPRING_SPEC
                            )
                            isClockwise = true
                        } else {
                            // Collapse effect
                            mainButtonScale.animateTo(
                                targetValue = 1.3f,
                                animationSpec = tween(
                                    AnimationConstants.SCALE_ANIMATION_DURATION,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            mainButtonScale.animateTo(
                                targetValue = AnimationConstants.EXPANDED_SCALE,
                                animationSpec = AnimationConstants.COLLAPSE_SPRING_SPEC
                            )
                            toggleChildAnimation++

                            isClockwise = false
                        }
                    }

                    launch {
                        rotationTrigger++
                    }
                }
            },
            containerColor = mainFabBackgroundColor,
            contentColor = mainFabContentColor,
            modifier = Modifier.scale(mainButtonScale.value)
        ) {
            RotatedIcon(
                modifier = Modifier.size(24.dp),
                icon = mainIcon,
                contentDescription = "Setting menu",
                rotationTrigger = rotationTrigger,
                isClockwise = isClockwise
            )
        }
    }
}
