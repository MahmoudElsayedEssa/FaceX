package com.example.facex.ui.screens.camera_face_recognition.components.fabs

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.facex.ui.components.icons.animated.AnimatedWrenchIcon
import com.example.facex.ui.components.icons.animated.JumpingIcon


@Composable
fun ExpandableFabGroup(
    modifier: Modifier = Modifier,
    onControlModelsClick: () -> Unit = {},
    onMetricsClick: () -> Unit = {}
) {
    val items = listOf(FabContentItem { toggleAnimation ->
        SmallShakingFabOnComplete(
            onClick = onMetricsClick,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            content = { onComplete ->
                JumpingIcon(
                    modifier = Modifier.size(16.dp),
                    icon = Icons.Default.Info,
                    contentDescription = "Performance Metrics",
                    delay = AnimationConstants.ICON_START_DELAY,
                    triggerAnimation = toggleAnimation,
                    onAnimationComplete = onComplete
                )
            },
        )
    },
        FabContentItem { toggleAnimation ->
            SmallFloatingActionButton(
                onClick = onControlModelsClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            ) {
                AnimatedWrenchIcon(
                    modifier = Modifier.size(16.dp),
                    contentDescription = "Models Control",
                    delay = AnimationConstants.ICON_START_DELAY,
                    jumpingToggle = toggleAnimation
                )

            }
        })

    ExpandableActionFab(
        modifier = modifier, items = items
    )
}



