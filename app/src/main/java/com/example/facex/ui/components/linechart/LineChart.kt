package com.example.facex.ui.components.linechart

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEach
import com.example.facex.domain.entities.Timestamp
import com.example.facex.domain.entities.Value
import com.example.facex.ui.components.linechart.ChartUtils.findNearestPoint
import com.example.facex.ui.components.linechart.ChartUtils.toChartPoints
import com.example.facex.ui.components.linechart.models.ChartConfig
import com.example.facex.ui.components.linechart.models.ChartDefaults.BASE_SKIP_AMOUNT
import com.example.facex.ui.components.linechart.models.ChartDefaults.DOUBLE_TAP_DISTANCE
import com.example.facex.ui.components.linechart.models.ChartDefaults.DOUBLE_TAP_TIMEOUT
import com.example.facex.ui.components.linechart.models.ChartDefaults.MAX_ZOOM
import com.example.facex.ui.components.linechart.models.ChartDefaults.MIN_VIEWPORT_DURATION
import com.example.facex.ui.components.linechart.models.ChartDefaults.MIN_ZOOM
import com.example.facex.ui.components.linechart.models.ChartDefaults.SKIP_MODE_TIMEOUT
import com.example.facex.ui.components.linechart.models.ChartState
import com.example.facex.ui.components.linechart.models.ChartTheme
import com.example.facex.ui.components.linechart.models.ChartViewport
import com.example.facex.ui.components.linechart.models.DataBounds
import com.example.facex.ui.components.linechart.models.InteractionMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun LineChart(
    data: Map<Timestamp, Value>, modifier: Modifier = Modifier, config: ChartConfig = ChartConfig()
) {
    val points = remember(data) { data.toChartPoints() }
    val scope = rememberCoroutineScope()
    val skipState = remember { mutableStateOf(SkipState()) }
    val density = LocalDensity.current

    if (points.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    var isAutoScrolling by remember { mutableStateOf(false) }

    val interactionState = remember {
        InteractionState(
            enableDoubleTapZoom = config.enableDoubleTapZoom,
            pointSelectionRadius = config.pointSelectionRadius
        )
    }

    var chartState by remember {
        mutableStateOf(
            ChartState(
                viewport = ChartViewport.createDefault(points),
                dataBounds = DataBounds(
                    minTime = points.first().timestamp,
                    maxTime = points.last().timestamp,
                    minValue = 0.0,
                    maxValue = points.maxOf { it.value },
                    points = points
                ),
                zoomLevel = config.initialZoom,
            )
        )
    }

    val visiblePoints by remember(points, chartState.viewport, chartState.zoomLevel) {
        derivedStateOf {
            val scaledDuration = (chartState.viewport.duration / chartState.zoomLevel).toLong()
            points.filter { point ->
                point.timestamp in chartState.viewport.startTime..(chartState.viewport.startTime + scaledDuration)
            }
        }
    }


    val isNearEnd = remember(chartState.viewport, points, chartState.zoomLevel) {
        if (points.isEmpty()) false
        else {
            val lastPointTime = points.last().timestamp
            val viewportEndTime = chartState.viewport.startTime + chartState.viewport.duration
            val maxDifference = 800L

            val timeDifference = (lastPointTime - viewportEndTime)

            timeDifference in -maxDifference..maxDifference
        }
    }
    LaunchedEffect(points) {
        val newDataBounds = DataBounds(
            minTime = points.first().timestamp,
            maxTime = points.last().timestamp,
            minValue = 0.0,
            maxValue = points.maxOf { it.value },
            points = points
        )

        if (isAutoScrolling) {
            val latestPoint = points.last()
            val duration = chartState.viewport.duration
            chartState = chartState.copy(
                dataBounds = newDataBounds, viewport = chartState.viewport.copy(
                    startTime = latestPoint.timestamp - duration,
                )
            )
        } else {
            // Update the data bounds even when not auto-scrolling
            chartState = chartState.copy(dataBounds = newDataBounds)
        }
    }

    LaunchedEffect(isNearEnd) {
        // Only allow toggling auto-scroll after a cooldown period
        isAutoScrolling = isNearEnd && !isAutoScrolling
    }

    val chartDrawer = rememberChartDrawer(
        state = chartState, config = config
    )
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        Column {
            LiveFeedControl(
                isAutoScrolling = isAutoScrolling,
                onToggle = {
                    chartState = if (!isAutoScrolling) {
                        val latestPoint = points.last()
                        val duration = chartState.viewport.duration
                        isAutoScrolling = isAutoScrolling && isNearEnd
                        chartState.copy(
                            viewport = chartState.viewport.copy(
                                startTime = latestPoint.timestamp - duration,
                            )
                        )
                    } else {
                        isAutoScrolling = false
                        chartState.copy()
                    }
                },
                theme = config.theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
            )

            Canvas(modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    chartState = chartState.copy(size = size.toSize())
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        if (skipState.value.isActive) return@awaitEachGesture

                        isAutoScrolling = isAutoScrolling && isNearEnd

                        var zoom = 1f
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop

                        // Store initial values
                        val initialDuration = chartState.viewport.duration
                        val viewportCenterTime =
                            chartState.viewport.startTime + (initialDuration / 2)

                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val touches = event.changes

                            if (touches.size == 2) {
                                val zoomChange = event.calculateZoom()
                                if (!pastTouchSlop) {
                                    zoom *= zoomChange
                                    val centroidSize =
                                        event.calculateCentroidSize(useCurrent = false)
                                    val zoomMotion = abs(1 - zoom) * centroidSize

                                    if (zoomMotion > touchSlop) {
                                        pastTouchSlop = true
                                    }
                                }

                                if (pastTouchSlop) {
                                    val newScale = (chartState.zoomLevel * zoomChange).coerceIn(
                                        MIN_ZOOM, MAX_ZOOM
                                    )

                                    // Calculate new duration with minimum bound
                                    val newDuration = (initialDuration / zoomChange).toLong()
                                        .coerceAtLeast(MIN_VIEWPORT_DURATION)

                                    // Calculate the new start time while keeping the center point
                                    val halfDuration = newDuration / 2
                                    val newStartTime = (viewportCenterTime - halfDuration).coerceIn(
                                        minimumValue = chartState.dataBounds.minTime,
                                        maximumValue = (chartState.dataBounds.maxTime - newDuration).coerceAtLeast(
                                            chartState.dataBounds.minTime
                                        )
                                    )

                                    // Only update if the new duration fits within the data bounds
                                    if (newStartTime + newDuration <= chartState.dataBounds.maxTime) {
                                        chartState = chartState.copy(
                                            zoomLevel = newScale,
                                            viewport = chartState.viewport.copy(
                                                startTime = newStartTime, zoomLevel = newScale
                                            )
                                        )
                                    }

                                    touches.fastForEach { it.consume() }
                                }
                            }
                        } while (touches.any { it.pressed })
                    }
                }

                // Update the pan gesture handler
                .pointerInput(Unit) {
                    awaitEachGesture {
                        if (skipState.value.isActive) return@awaitEachGesture

                        isAutoScrolling = isAutoScrolling && isNearEnd

                        val down = awaitFirstDown(requireUnconsumed = false)
                        var lastPosition = down.position.x

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()

                            if (change.pressed) {
                                val dragAmount = change.position.x - lastPosition
                                lastPosition = change.position.x

                                val timeRange = chartState.viewport.duration
                                val pixelToTimeRatio = timeRange.toFloat() / size.width
                                val timeDelta = (dragAmount * pixelToTimeRatio).toLong()

                                change.consume()

                                // Calculate new start time with bounds checking
                                val newStartTime =
                                    (chartState.viewport.startTime + timeDelta).coerceIn(
                                        minimumValue = chartState.dataBounds.minTime,
                                        maximumValue = (chartState.dataBounds.maxTime - chartState.viewport.duration).coerceAtLeast(
                                            chartState.dataBounds.minTime
                                        )
                                    )

                                chartState = chartState.copy(
                                    viewport = chartState.viewport.copy(
                                        startTime = newStartTime,
                                    )
                                )
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapPosition ->
                        scope.launch(Dispatchers.Default) {
                            val currentTime = System.currentTimeMillis()
                            val isDoubleTap = with(density) {
                                val (lastTime, lastPosition) = interactionState.lastTapInfo

                                val doubleTapDistance = DOUBLE_TAP_DISTANCE

                                (currentTime - lastTime) < DOUBLE_TAP_TIMEOUT && (tapPosition - lastPosition).getDistance() < doubleTapDistance.dp.toPx()
                            }

                            when {
                                // Handle double tap to enter skip mode
                                isDoubleTap && interactionState.interactionMode is InteractionMode.Normal -> {
                                    interactionState.interactionMode =
                                        InteractionMode.SkipNavigation
                                    interactionState.skipDirection =
                                        if (tapPosition.x > chartState.size.width / 2) {
                                            SkipDirection.FORWARD
                                        } else {
                                            SkipDirection.BACKWARD
                                        }
                                    interactionState.skipCount = 0
                                    interactionState.showIndicator = true
                                }

                                // Handle skip navigation
                                interactionState.interactionMode is InteractionMode.SkipNavigation -> {
                                    val lastTapTime = interactionState.lastTapInfo.first
                                    val timeSinceLastTap = currentTime - lastTapTime
                                    val sideThreshold =
                                        chartState.size.width * 0.25f  // 25% from each side

                                    if (timeSinceLastTap > SKIP_MODE_TIMEOUT) {
                                        // Reset skip navigation if too much time has passed
                                        interactionState.interactionMode = InteractionMode.Normal
                                        interactionState.skipCount = 0
                                        interactionState.showIndicator = false
                                    } else {
                                        // Check if tap is in skip areas (sides of screen)
                                        val isInSkipArea =
                                            tapPosition.x < sideThreshold || tapPosition.x > (chartState.size.width - sideThreshold)

                                        if (isInSkipArea) {
                                            // Handle skip navigation
                                            isAutoScrolling = false


                                            val isRightSide =
                                                tapPosition.x > chartState.size.width / 2
                                            val newDirection = if (isRightSide) {
                                                SkipDirection.FORWARD
                                            } else {
                                                SkipDirection.BACKWARD
                                            }

                                            interactionState.skipCount =
                                                if (newDirection == interactionState.skipDirection) {
                                                    interactionState.skipCount + 1
                                                } else {
                                                    interactionState.skipDirection = newDirection
                                                    0
                                                }

                                            val skipAmount =
                                                BASE_SKIP_AMOUNT * (interactionState.skipCount + 1)
                                            val timeDelta =
                                                if (newDirection == SkipDirection.FORWARD) skipAmount else -skipAmount

                                            // Inside the skip navigation handling
                                            val newStartTime =
                                                chartState.viewport.startTime + timeDelta
                                            val maxStartTime =
                                                (chartState.dataBounds.maxTime - chartState.viewport.duration).coerceAtLeast(
                                                    chartState.dataBounds.minTime
                                                )

                                            val constrainedStartTime = newStartTime.coerceIn(
                                                minimumValue = chartState.dataBounds.minTime,
                                                maximumValue = maxStartTime
                                            )

                                            chartState = chartState.copy(
                                                viewport = chartState.viewport.copy(
                                                    startTime = constrainedStartTime,
                                                )
                                            )

                                            interactionState.showIndicator = true
                                            scope.launch {
                                                delay(800L)
                                                interactionState.showIndicator = false
                                            }
                                        } else {
                                            // Handle point selection if tap is in center area
                                            if (visiblePoints.isNotEmpty()) {
                                                val pointSelectionThreshold =
                                                    with(density) { 16.dp.toPx() }
                                                val pinnedPoint = visiblePoints.findNearestPoint(
                                                    position = tapPosition,
                                                    viewport = chartState.viewport,
                                                    size = chartState.size,
                                                    density = density,
                                                    contentPadding = config.dimensions.contentPadding,
                                                    threshold = pointSelectionThreshold
                                                )
                                                chartState =
                                                    chartState.copy(pinnedPoint = pinnedPoint)
                                            }
                                        }
                                    }
                                }
                                // Handle normal tap for point selection
                                else -> {
                                    if (visiblePoints.isNotEmpty()) {
                                        val pointSelectionThreshold = with(density) { 16.dp.toPx() }
                                        val pinnedPoint = visiblePoints.findNearestPoint(
                                            position = tapPosition,
                                            viewport = chartState.viewport,
                                            size = chartState.size,
                                            density = density,
                                            contentPadding = config.dimensions.contentPadding,
                                            threshold = pointSelectionThreshold
                                        )
                                        chartState = chartState.copy(pinnedPoint = pinnedPoint)
                                    }
                                }
                            }

                            interactionState.lastTapInfo = currentTime to tapPosition
                        }
                    }
                }

            ) {
                chartDrawer.draw(
                    scope = this,
                    chartState = chartState,
                    visiblePoints = visiblePoints,
                    textMeasurer = textMeasurer,
                )

            }
        }

        AnimatedSkipIndicator(
            interactionState = interactionState, theme = config.theme
        )
    }
}


@Composable
private fun AnimatedSkipIndicator(
    interactionState: InteractionState, theme: ChartTheme
) {
    // Main transition for the indicator's visibility
    val transition = updateTransition(
        targetState = interactionState.showIndicator && interactionState.interactionMode is InteractionMode.SkipNavigation,
        label = "skipIndicator"
    )

    val alpha by transition.animateFloat(label = "opacity",
        transitionSpec = {
            tween(
                200,
                easing = FastOutSlowInEasing
            )
        }) { visible -> if (visible) 1f else 0f }

    Box(modifier = Modifier.fillMaxSize()) {
        if (transition.currentState || transition.targetState) {
            SkipAnimationIndicator(
                duration = 1500L * (interactionState.skipCount + 1),
                isForward = interactionState.skipDirection == SkipDirection.FORWARD,
                theme = theme,
                alpha = alpha,
                modifier = Modifier
                    .align(
                        if (interactionState.skipDirection == SkipDirection.FORWARD) Alignment.CenterEnd
                        else Alignment.CenterStart
                    )
                    .padding(horizontal = 32.dp)
            )
        }
    }
}


@Composable
private fun EmptyChartPlaceholder(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}


@Composable
private fun rememberChartDrawer(
    state: ChartState, config: ChartConfig
): ChartDrawer {
    return remember(state, config) {
        ChartDrawer(config = config)
    }
}



