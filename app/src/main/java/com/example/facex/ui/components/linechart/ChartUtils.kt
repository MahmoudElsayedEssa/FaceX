package com.example.facex.ui.components.linechart

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.facex.ui.components.linechart.models.ChartPoint
import com.example.facex.ui.components.linechart.models.ChartViewport


object ChartUtils {

    fun Map<Long, Number>.toChartPoints(): List<ChartPoint> = entries.sortedBy { it.key }
        .map { (timestamp, value) -> ChartPoint(timestamp, value.toDouble()) }

    fun PaddingValues.calculateContentArea(scope: DrawScope) = with(scope) {
        Rect(
            left = calculateLeftPadding(LayoutDirection.Ltr).toPx(),
            top = calculateTopPadding().toPx(),
            right = size.width - calculateRightPadding(LayoutDirection.Ltr).toPx(),
            bottom = size.height - calculateBottomPadding().toPx()
        )
    }


    fun List<ChartPoint>.findNearestPoint(
        position: Offset,
        viewport: ChartViewport,
        size: Size,
        density: Density,
        contentPadding: PaddingValues,
        threshold: Float
    ): ChartPoint? {
        if (isEmpty()) return null

        with(density) {
            // Calculate content area bounds
            val contentArea = Rect(
                left = contentPadding.calculateLeftPadding(LayoutDirection.Ltr).toPx(),
                top = contentPadding.calculateTopPadding().toPx(),
                right = size.width - contentPadding.calculateRightPadding(LayoutDirection.Ltr).toPx(),
                bottom = size.height - contentPadding.calculateBottomPadding().toPx()
            )

            // Check if tap is within chart bounds
            if (!contentArea.contains(position)) return null

            // Calculate adjusted time based on zoom level
            val visibleDuration = viewport.duration / viewport.zoomLevel
            val timeRatio = (position.x - contentArea.left) / contentArea.width
            val tappedTime = viewport.startTime + (visibleDuration * timeRatio).toLong()

            // Calculate visible value range based on zoom
            val visibleMinValue = viewport.minValue
            val visibleMaxValue = viewport.minValue +
                    ((viewport.maxValue - viewport.minValue) / viewport.zoomLevel)

            // Find points within time window considering zoom level
            val timeWindow = (visibleDuration * (threshold / contentArea.width)).toLong()
            val candidatePoints = filter { point ->
                point.timestamp in (tappedTime - timeWindow)..(tappedTime + timeWindow)
            }

            if (candidatePoints.isEmpty()) return null

            // Find nearest point considering both time and value
            return candidatePoints
                .map { point ->
                    val pointPosition = calculatePointPosition(
                        point = point,
                        contentArea = contentArea,
                        viewport = viewport,
                        visibleDuration = visibleDuration,
                        visibleMinValue = visibleMinValue,
                        visibleMaxValue = visibleMaxValue
                    )
                    point to (pointPosition - position).getDistance()
                }
                .filter { (_, distance) -> distance <= threshold }
                .minByOrNull { (_, distance) -> distance }
                ?.first
        }
    }


    fun calculatePointPosition(
        point: ChartPoint,
        contentArea: Rect,
        viewport: ChartViewport,
        visibleDuration: Float,
        visibleMinValue: Double,
        visibleMaxValue: Double
    ): Offset {
        // X position with zoom consideration
        val xRatio = (point.timestamp - viewport.startTime).toFloat() / visibleDuration
        val x = contentArea.left + (xRatio * contentArea.width)

        // Y position with zoom consideration
        val yRatio = (point.value - visibleMinValue) / (visibleMaxValue - visibleMinValue)
        val y = contentArea.bottom - (yRatio * contentArea.height)

        return Offset(x, y.toFloat())
    }
}
