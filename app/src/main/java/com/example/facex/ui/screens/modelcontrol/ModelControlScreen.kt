import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.facex.domain.entities.ServiceOption
import com.example.facex.ui.components.AnimatedSelectableQueue
import com.example.facex.ui.screens.modelcontrol.ModelControlActions
import com.example.facex.ui.screens.modelcontrol.ModelControlState
import com.example.facex.ui.screens.modelcontrol.components.ServiceCard


@Composable
fun ModelControlScreen(
    state: ModelControlState,
    actions: ModelControlActions,
    expandedRatioProvider: () -> Float = { 1f }
) {
    val expandedRatio by remember { derivedStateOf { expandedRatioProvider() } }

    Column(modifier = Modifier.fillMaxSize()) {
        DetectionServicesSection(
            services = state.detectionServices,
            selectedIndex = state.selectedDetectionIndex,
            expandedRatio = expandedRatio,
            actions = actions
        )

        RecognitionServicesSection(
            services = state.recognitionServices,
            selectedIndex = state.selectedRecognitionIndex,
            expandedRatio = expandedRatio,
            actions = actions
        )
    }
}

@Composable
fun DetectionServicesSection(
    services: List<ServiceOption>,
    selectedIndex: Int,
    expandedRatio: Float,
    actions: ModelControlActions,
) {
    var detectionSelectedIndex by remember { mutableIntStateOf(selectedIndex) }

    Text(
        text = "Face Detection",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    AnimatedSelectableQueue(
        items = services,
        key = { item -> item.id },
        visibilityRatio = expandedRatio,
        selectedIndex = detectionSelectedIndex,
        onTopItemSelected = { selectedItem ->
            detectionSelectedIndex = -1
            actions.moveDetectionServiceToTop(selectedItem)
        },
        itemContent = { item, itemPhase, isAnimating, index ->

            ServiceCard(
                option = item,
                itemPhase = itemPhase,
                modifier = Modifier.fillMaxWidth(),
                onSaveChanges = { model, newThreshold, acceleration ->
                    actions.onDetectionChangeThreshold(item, newThreshold)
                    actions.onChangeAcceleration(item, acceleration)
                },
                onChangeService = {
                    actions.onSwitchDetectionService(item)
                    detectionSelectedIndex = index
                },
            )
        })

}

@Composable
fun RecognitionServicesSection(
    services: List<ServiceOption>,
    selectedIndex: Int,
    expandedRatio: Float,
    actions: ModelControlActions,
) {
    var recognitionSelectedIndex by remember { mutableIntStateOf(selectedIndex) }

    Text(
        text = "Face Recognition",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    AnimatedSelectableQueue(
        items = services,
        key = { item -> item.id },
        onTopItemSelected = { selectedItem ->
            recognitionSelectedIndex = -1
            actions.moveRecognitionServiceToTop(selectedItem)
        },
        visibilityRatio = expandedRatio,
        selectedIndex = recognitionSelectedIndex,
        itemContent = { item, phase, _, index ->
            ServiceCard(
                option = item,
                modifier = Modifier.fillMaxWidth(),
                onSaveChanges = { model, newThreshold, acceleration ->
                    actions.onRecognitionChangeThreshold(item, newThreshold)
                    actions.onChangeAcceleration(item, acceleration)
                },
                itemPhase = phase,
                onChangeService = {
                    actions.onSwitchRecognitionService(item)
                    recognitionSelectedIndex = index
                })
        }
    )
}
