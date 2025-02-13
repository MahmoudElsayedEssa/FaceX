package com.example.facex.ui.components.linechart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facex.ui.components.linechart.ChartUtils.calculateContentArea
import com.example.facex.ui.components.linechart.models.ChartConfig
import com.example.facex.ui.components.linechart.models.ChartDefaults.DEFAULT_CONTENT_PADDING
import com.example.facex.ui.components.linechart.models.ChartPoint
import com.example.facex.ui.components.linechart.models.ChartState
import com.example.facex.ui.components.linechart.models.ChartStyle
import com.example.facex.ui.components.linechart.models.ChartViewport
import com.example.facex.ui.components.linechart.models.toStyle

class ChartDrawer(private val config: ChartConfig) {
    private data class DrawingContext(
        val scope: DrawScope,
        val contentArea: Rect,
        val style: ChartStyle,
        val viewport: ChartViewport,
        val textMeasurer: TextMeasurer
    )

    private val padding = DEFAULT_CONTENT_PADDING
    private val dashedPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    fun draw(
        scope: DrawScope,
        chartState: ChartState,
        visiblePoints: List<ChartPoint>,
        textMeasurer: TextMeasurer,

        ) {
        val context = createContext(scope, textMeasurer, chartState.viewport)
        drawChart(context, chartState, visiblePoints)
    }

    private fun createContext(
        scope: DrawScope, textMeasurer: TextMeasurer, viewport: ChartViewport
    ): DrawingContext {
        val contentArea = padding.calculateContentArea(scope)
        val style = config.theme.toStyle()
        return DrawingContext(scope, contentArea, style, viewport, textMeasurer)
    }

    private fun drawChart(
        context: DrawingContext, chartState: ChartState, visiblePoints: List<ChartPoint>
    ) = with(context.scope) {
        drawAxes(context)

        clipRect(
            context.contentArea.left,
            context.contentArea.top,
            context.contentArea.right,
            context.contentArea.bottom
        ) {
            drawGrid(context)
            drawData(context, chartState, visiblePoints)
            chartState.pinnedPoint?.let { drawPinnedPoint(context, it) }
        }
        drawLabels(context)

    }

    private fun DrawScope.drawAxes(context: DrawingContext) {
        drawLine(
            color = context.style.labelColor,
            start = Offset(context.contentArea.left, context.contentArea.top),
            end = Offset(context.contentArea.left, context.contentArea.bottom),
            strokeWidth = 1.dp.toPx()
        )

        drawLine(
            color = context.style.labelColor,
            start = Offset(context.contentArea.left, context.contentArea.bottom),
            end = Offset(context.contentArea.right, context.contentArea.bottom),
            strokeWidth = 1.dp.toPx()
        )
    }

    private fun DrawScope.drawGrid(context: DrawingContext) {
        drawHorizontalGridLines(context)
        drawVerticalGridLines(context)
    }

    private fun DrawScope.drawHorizontalGridLines(context: DrawingContext) {
        val (minValue, maxValue) = calculateVisibleRange(context.viewport)

        (0..config.dimensions.ySteps).forEach { step ->
            val value =
                minValue + ((maxValue - minValue) * (step.toFloat() / config.dimensions.ySteps))
            val y = calculateY(value, context)

            if (y < context.contentArea.bottom) {
                drawDashedLine(
                    context,
                    Offset(context.contentArea.left, y),
                    Offset(context.contentArea.right, y)
                )
            }
        }
    }

    private fun DrawScope.drawVerticalGridLines(context: DrawingContext) {
        val visibleDuration = context.viewport.duration / context.viewport.zoomLevel

        (0..config.dimensions.xSteps).forEach { step ->
            val time =
                context.viewport.startTime + ((visibleDuration) * (step.toFloat() / config.dimensions.xSteps))
            val x = calculateX(time.toLong(), context)

            if (x > context.contentArea.left) {
                drawDashedLine(
                    context,
                    Offset(x, context.contentArea.top),
                    Offset(x, context.contentArea.bottom)
                )
            }
        }
    }

    private fun DrawScope.drawDashedLine(context: DrawingContext, start: Offset, end: Offset) {
        drawLine(
            color = context.style.gridColor,
            start = start,
            end = end,
            strokeWidth = config.dimensions.gridLineWidth.toPx(),
            pathEffect = dashedPathEffect
        )
    }

    private fun DrawScope.drawData(
        context: DrawingContext, chartState: ChartState, visiblePoints: List<ChartPoint>
    ) {
        if (visiblePoints.isEmpty()) return

        drawGradientFill(context, chartState, visiblePoints)
        drawDataLine(context, chartState, visiblePoints)
        drawDataPoints(context, chartState, visiblePoints)
    }

    private fun cubicBezier(
        start: Offset,
        control1: Offset,
        control2: Offset,
        end: Offset,
        t: Float
    ): Offset {
        val oneMinusT = 1f - t
        val x = oneMinusT * oneMinusT * oneMinusT * start.x +
                3 * oneMinusT * oneMinusT * t * control1.x +
                3 * oneMinusT * t * t * control2.x +
                t * t * t * end.x
        val y = oneMinusT * oneMinusT * oneMinusT * start.y +
                3 * oneMinusT * oneMinusT * t * control1.y +
                3 * oneMinusT * t * t * control2.y +
                t * t * t * end.y
        return Offset(x, y)
    }

    private fun DrawScope.drawGradientFill(
        context: DrawingContext,
        chartState: ChartState,
        visiblePoints: List<ChartPoint>
    ) {
        val path = createDataPath(context, chartState, visiblePoints)

        // Get the last point (either the next invisible point or the last visible point)
        val allPoints = chartState.dataBounds.points
        val lastVisiblePointIndex = allPoints.indexOf(visiblePoints.last())
        val endPoint = if (lastVisiblePointIndex < allPoints.lastIndex) {
            allPoints[lastVisiblePointIndex + 1]
        } else visiblePoints.last()

        val endPosition = calculatePosition(endPoint, context)

        path.apply {
            lineTo(endPosition.x, context.contentArea.bottom)
            lineTo(context.contentArea.left, context.contentArea.bottom)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = context.style.gradientColors,
                startY = context.contentArea.top,
                endY = context.contentArea.bottom
            )
        )
    }

    private fun DrawScope.drawDataLine(
        context: DrawingContext,
        chartState: ChartState,
        visiblePoints: List<ChartPoint>,
        // New parameter: when a new point is arriving, animate this from 0f to 1f.
        newSegmentAnimationProgress: Float = 1f
    ) {
        if (visiblePoints.isEmpty()) return

        // If we have only one point or the new segment is fully drawn, just draw the complete path:
        if (visiblePoints.size < 2 || newSegmentAnimationProgress >= 1f) {
            val path = createDataPath(context, chartState, visiblePoints)
            drawPath(
                path = path,
                color = context.style.lineColor,
                style = Stroke(
                    width = config.dimensions.lineWidth.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = if (visiblePoints.zipWithNext().all { (p1, p2) ->
                            chartState.dataBounds.points.indexOf(p2) - chartState.dataBounds.points.indexOf(
                                p1
                            ) == 1
                        }) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
            return
        }

        // Otherwise, draw the static part (all segments except the last one)
        if (visiblePoints.size >= 2) {
            val staticPoints = visiblePoints.dropLast(1)
            if (staticPoints.isNotEmpty()) {
                val staticPath = Path().apply {
                    val startPos = calculatePosition(staticPoints.first(), context)
                    moveTo(startPos.x, startPos.y)
                    var previousPos = startPos
                    staticPoints.drop(1).forEach { point ->
                        val currentPos = calculatePosition(point, context)
                        // Compute a simple control point for a smooth curve
                        val controlX = (previousPos.x + currentPos.x) / 2f
                        cubicTo(
                            controlX,
                            previousPos.y,
                            controlX,
                            currentPos.y,
                            currentPos.x,
                            currentPos.y
                        )
                        previousPos = currentPos
                    }
                }
                drawPath(
                    path = staticPath,
                    color = context.style.lineColor,
                    style = Stroke(
                        width = config.dimensions.lineWidth.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Animate the last segment from the second-to-last point to the new (last) point.
            val previousPoint = visiblePoints[visiblePoints.size - 2]
            val newPoint = visiblePoints.last()
            val startPos = calculatePosition(previousPoint, context)
            val endPos = calculatePosition(newPoint, context)
            val controlX = (startPos.x + endPos.x) / 2f
            val control1 = Offset(controlX, startPos.y)
            val control2 = Offset(controlX, endPos.y)

            // Compute the intermediate point along the cubic Bézier curve using the animation progress.
            val animatedPoint =
                cubicBezier(startPos, control1, control2, endPos, newSegmentAnimationProgress)

            // Draw a path from the start of the segment to the animated point.
            val animatedPath = Path().apply {
                moveTo(startPos.x, startPos.y)
                // For a very simple animated effect, we use a line.
                // (You could also rebuild a Bézier path up to the animated point if you prefer.)
                lineTo(animatedPoint.x, animatedPoint.y)
            }
            drawPath(
                path = animatedPath,
                color = context.style.lineColor,
                style = Stroke(
                    width = config.dimensions.lineWidth.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }


    private fun createDataPath(
        context: DrawingContext,
        visiblePoints: List<ChartPoint>
    ): Path {
        val path = Path()
        if (visiblePoints.isEmpty()) return path

        // Start at the first visible point.
        val firstPosition = calculatePosition(visiblePoints.first(), context)
        path.moveTo(firstPosition.x, firstPosition.y)
        var previousPosition = firstPosition

        // Function to create a smooth curve between points.
        fun addSmoothCurve(from: Offset, to: Offset) {
            val controlX = (from.x + to.x) / 2f
            path.cubicTo(
                controlX, from.y,
                controlX, to.y,
                to.x, to.y
            )
        }

        // Draw smooth curves only between visible points.
        visiblePoints.drop(1).forEach { point ->
            val currentPosition = calculatePosition(point, context)
            addSmoothCurve(previousPosition, currentPosition)
            previousPosition = currentPosition
        }

        return path
    }

    private fun createDataPath(
        context: DrawingContext,
        chartState: ChartState,
        visiblePoints: List<ChartPoint>
    ): Path {
        val path = Path()
        var firstPoint = true

        // Get all points
        val allPoints = chartState.dataBounds.points

        // Ensure points are consecutive
        val isConsecutive = visiblePoints.zipWithNext().all { (point1, point2) ->
            val index1 = allPoints.indexOf(point1)
            val index2 = allPoints.indexOf(point2)
            index2 - index1 == 1
        }

        if (!isConsecutive) {
            // If points are not consecutive, draw them separately
            visiblePoints.forEach { point ->
                val position = calculatePosition(point, context)
                path.moveTo(position.x, position.y)
            }
            return path
        }

        // Find indices of visible points range
        val firstVisiblePointIndex = allPoints.indexOf(visiblePoints.first())
        val lastVisiblePointIndex = allPoints.indexOf(visiblePoints.last())

        // Get invisible points from both ends if they exist
        val previousPoint = if (firstVisiblePointIndex > 0) {
            allPoints[firstVisiblePointIndex - 1]
        } else null

        val nextPoint = if (lastVisiblePointIndex < allPoints.lastIndex) {
            allPoints[lastVisiblePointIndex + 1]
        } else null


        // Start with previous invisible point if exists
        previousPoint?.let {
            val position = calculatePosition(it, context)
            path.moveTo(position.x, position.y)
            firstPoint = false
        }

        // Draw visible points
        visiblePoints.forEach { point ->
            val position = calculatePosition(point, context)
            if (firstPoint) {
                path.moveTo(position.x, position.y)
                firstPoint = false
            } else {
                path.lineTo(position.x, position.y)
            }
        }

        // Add next invisible point if exists
        nextPoint?.let {
            val position = calculatePosition(it, context)
            path.lineTo(position.x, position.y)
        }

        return path
    }

    private fun DrawScope.drawDataPoints(
        context: DrawingContext, chartState: ChartState, visiblePoints: List<ChartPoint>
    ) {
        visiblePoints.forEach { point ->
            if (point != chartState.pinnedPoint) {
                val position = calculatePosition(point, context)
                drawCircle(
                    color = context.style.lineColor,
                    radius = config.dimensions.pointRadius.toPx(),
                    center = position
                )
            }
        }
    }

    private fun DrawScope.drawLabels(context: DrawingContext) {
        drawYAxisLabels(context)
        drawXAxisLabels(context)
    }

    private fun DrawScope.drawYAxisLabels(context: DrawingContext) {
        val (minValue, maxValue) = calculateVisibleRange(context.viewport)

        (0..config.dimensions.ySteps).forEach { step ->
            val value =
                maxValue - ((maxValue - minValue) * (step.toFloat() / config.dimensions.ySteps))
            val y =
                context.contentArea.top + (context.contentArea.height * (step.toFloat() / config.dimensions.ySteps))

            val text = config.formatters.valueFormatter(value)
            val textLayout = context.textMeasurer.measure(text)

            drawText(
                textLayoutResult = textLayout, color = context.style.labelColor, topLeft = Offset(
                    x = context.contentArea.left - textLayout.size.width - 8.dp.toPx(),
                    y = y - textLayout.size.height / 2
                )
            )
        }
    }

    private fun DrawScope.drawXAxisLabels(context: DrawingContext) {
        val visibleDuration = context.viewport.duration / context.viewport.zoomLevel
        val availableWidthPerLabel = context.contentArea.width / config.dimensions.xSteps

        (0..config.dimensions.xSteps).forEach { step ->
            val time =
                context.viewport.startTime + ((visibleDuration) * (step.toFloat() / config.dimensions.xSteps))
            val x = calculateX(time.toLong(), context)

            val text = formatTimeLabel(time.toLong(), step, context, availableWidthPerLabel)
            val textLayout = context.textMeasurer.measure(text)

            drawText(
                textLayoutResult = textLayout, color = context.style.labelColor, topLeft = Offset(
                    x = x - textLayout.size.width / 2, y = context.contentArea.bottom + 8.dp.toPx()
                )
            )
        }
    }

    private fun formatTimeLabel(
        time: Long, step: Int, context: DrawingContext, availableWidth: Float
    ): String {
        val fullText = config.formatters.timeFormatter(time)
        val fullTextLayout = context.textMeasurer.measure(fullText)

        return if (fullTextLayout.size.width <= availableWidth) {
            fullText
        } else {
            when (step) {
                0, config.dimensions.xSteps -> config.formatters.timeFormatter(time)
                else -> "..."
            }
        }
    }

    private fun DrawScope.drawPinnedPoint(context: DrawingContext, point: ChartPoint) {
        val position = calculatePosition(point, context)

        drawPinnedMarker(context, position)
        drawReferenceLines(context, position)
        drawPinnedLabels(context, position, point)
    }

    private fun DrawScope.drawPinnedMarker(context: DrawingContext, position: Offset) {
        // Pulse effect
        drawCircle(
            color = context.style.selectionColor.copy(alpha = 0.15f),
            radius = (config.dimensions.selectedPointRadius * 1.8f).toPx(),
            center = position
        )

        // Main circle
        drawCircle(
            color = context.style.backgroundColor,
            radius = config.dimensions.selectedPointRadius.toPx(),
            center = position
        )
        drawCircle(
            color = context.style.selectionColor,
            radius = config.dimensions.selectedPointRadius.toPx(),
            center = position,
            style = Stroke(width = 2.dp.toPx())
        )

        // Center dot
        drawCircle(
            color = context.style.selectionColor,
            radius = (config.dimensions.pointRadius * 0.8f).toPx(),
            center = position
        )
    }

    private fun DrawScope.drawReferenceLines(context: DrawingContext, position: Offset) {
        val referenceLineStyle = context.style.selectionColor.copy(alpha = 0.3f)

        // Y-axis marker
        drawLine(
            color = referenceLineStyle,
            start = Offset(context.contentArea.left + 5.dp.toPx(), position.y),
            end = Offset(context.contentArea.left, position.y),
            strokeWidth = 3.dp.toPx()
        )

        // Reference lines
        drawLine(
            color = referenceLineStyle,
            start = Offset(context.contentArea.left, position.y),
            end = Offset(position.x, position.y),
            strokeWidth = 2.dp.toPx(),
            pathEffect = dashedPathEffect
        )
        drawLine(
            color = referenceLineStyle,
            start = Offset(position.x, position.y),
            end = Offset(position.x, context.contentArea.bottom),
            strokeWidth = 2.dp.toPx(),
            pathEffect = dashedPathEffect
        )
    }

    private fun DrawScope.drawPinnedLabels(
        context: DrawingContext,
        position: Offset,
        point: ChartPoint
    ) {
        val textStyle = TextStyle(
            color = context.style.selectionColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        // Y-axis label
        val yText = config.formatters.valueFormatter(point.value)
        val yTextLayout = context.textMeasurer.measure(text = yText, style = textStyle)
        val yLabelX = (context.contentArea.left - yTextLayout.size.width + 32.dp.toPx())
            .coerceAtLeast(0f) // Ensure X position is not negative

        drawText(
            textMeasurer = context.textMeasurer,
            text = yText,
            topLeft = Offset(
                x = yLabelX,
                y = position.y - yTextLayout.size.height / 2 - 8.dp.toPx()
            ),
            style = textStyle
        )

        // X-axis label
        val xText = config.formatters.timeFormatter(point.timestamp)
        val xTextLayout = context.textMeasurer.measure(text = xText, style = textStyle)

        // Ensure the label stays within the chart bounds
        val xLabelX = (position.x - xTextLayout.size.width / 2 + 20.dp.toPx())
            .coerceIn(
                context.contentArea.left,
                context.contentArea.right - xTextLayout.size.width
            )

        drawText(
            textMeasurer = context.textMeasurer,
            text = xText,
            topLeft = Offset(
                x = xLabelX,
                y = context.contentArea.bottom - 16.dp.toPx()
            ),
            style = textStyle
        )
    }


    private fun calculatePosition(point: ChartPoint, context: DrawingContext): Offset {
        val xRatio =
            (point.timestamp - context.viewport.startTime).toFloat() / (context.viewport.duration / context.viewport.zoomLevel)

        val (minValue, maxValue) = calculateVisibleRange(context.viewport)
        val yRatio = (point.value - minValue) / (maxValue - minValue)

        return Offset(
            x = context.contentArea.left + (xRatio * context.contentArea.width),
            y = (context.contentArea.bottom - (yRatio * context.contentArea.height)).toFloat()
        )
    }

    private fun calculateX(time: Long, context: DrawingContext): Float {
        val xRatio =
            (time - context.viewport.startTime).toFloat() / (context.viewport.duration / context.viewport.zoomLevel)
        return context.contentArea.left + (xRatio * context.contentArea.width)
    }

    private fun calculateY(value: Double, context: DrawingContext): Float {
        val (minValue, maxValue) = calculateVisibleRange(context.viewport)
        val yRatio = (value - minValue) / (maxValue - minValue)
        return (context.contentArea.bottom - (yRatio * context.contentArea.height)).toFloat()
    }

    private fun calculateVisibleRange(viewport: ChartViewport): Pair<Double, Double> {
        val visibleMax =
            viewport.minValue + ((viewport.maxValue - viewport.minValue) / viewport.zoomLevel)
        return viewport.minValue to visibleMax
    }

}

