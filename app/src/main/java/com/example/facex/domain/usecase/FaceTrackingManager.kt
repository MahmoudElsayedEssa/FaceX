package com.example.facex.domain.usecase

import android.graphics.Point
import com.example.facex.domain.entities.Rectangle
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class FaceTrackingManager @Inject constructor(
    private val logger: Logger
) {
    companion object {
        private const val FACE_TRACKING_WINDOW = 10
        private const val MIN_DETECTION_COUNT = 6
        private const val TRACKING_TIME_WINDOW = 1000L
        private const val MAX_POSITION_VARIANCE = 0.1f // Maximum allowed position variance as fraction of frame
        private const val MIN_SIZE_RATIO = 0.05f // Minimum face size relative to frame
        private const val MAX_SIZE_RATIO = 0.8f // Maximum face size relative to frame
    }

    init {
        logger.tag = "FaceTrackingManager"
    }

    private data class FaceTrackingInfo(
        val recentDetections: ArrayDeque<Long> = ArrayDeque(FACE_TRACKING_WINDOW),
        val positions: ArrayDeque<Rectangle> = ArrayDeque(FACE_TRACKING_WINDOW),
        var frameWidth: Int = 0,
        var frameHeight: Int = 0
    ) {
        val detectionRate: Float
            get() = recentDetections.size.toFloat() / FACE_TRACKING_WINDOW

        fun calculatePositionStability(): Float {
            if (positions.size < 2) return 0f

            val centerPoints = positions.map { rect ->
                Point(
                    rect.x + rect.width / 2,
                    rect.y + rect.height / 2
                )
            }

            val averageX = centerPoints.map { it.x }.average()
            val averageY = centerPoints.map { it.y }.average()

            val maxVariance = (frameWidth + frameHeight) / 2 * MAX_POSITION_VARIANCE

            val averageVariance = centerPoints.map { point ->
                sqrt(
                    (point.x - averageX).pow(2) +
                            (point.y - averageY).pow(2)
                )
            }.average()

            return (1 - (averageVariance / maxVariance)).coerceIn(0.0, 1.0).toFloat()
        }

        fun isValidSize(rect: Rectangle): Boolean {
            val frameSize = minOf(frameWidth, frameHeight)
            val faceSize = minOf(rect.width, rect.height)
            val sizeRatio = faceSize.toFloat() / frameSize

            return sizeRatio in MIN_SIZE_RATIO..MAX_SIZE_RATIO
        }
    }

    private val faceTracker = mutableMapOf<Int, FaceTrackingInfo>()

//    fun isStableFace(
//        trackingId: Int,
//        boundingBox: Rectangle,
//        frameWidth: Int,
//        frameHeight: Int
//    ): Boolean {
//        val currentTimestamp = System.currentTimeMillis()
//        cleanOldDetections(currentTimestamp)
//
//        val trackingInfo = faceTracker.getOrPut(trackingId) {
//            FaceTrackingInfo().also {
//                it.frameWidth = frameWidth
//                it.frameHeight = frameHeight
//            }
//        }
//
//        // Size validation
//        if (!trackingInfo.isValidSize(boundingBox)) {
//            return false
//        }
//
//        // Update tracking info
//        trackingInfo.recentDetections.addLast(currentTimestamp)
//        trackingInfo.positions.addLast(boundingBox)
//
//        if (trackingInfo.recentDetections.size > FACE_TRACKING_WINDOW) {
//            trackingInfo.recentDetections.removeFirst()
//            trackingInfo.positions.removeFirst()
//        }
//
//        val detectionCount = trackingInfo.recentDetections.size
//        val detectionRate = trackingInfo.detectionRate
//        val positionStability = trackingInfo.calculatePositionStability()
//
//
//        val isStable = detectionCount >= MIN_DETECTION_COUNT &&
//                detectionRate >= 0.6f &&
//                positionStability >= 0.7f
//
//            "Face $trackingId stability: $isStable, " +
//                    "detectionCount: $detectionCount," +
//                    "detectionRate: $detectionRate," +
//                    "positionStability: $positionStability"
//                        .logDebug(logger)
//
//        return isStable
//    }

    private fun cleanOldDetections(currentTimestamp: Long) {
        val oldestAllowed = currentTimestamp - TRACKING_TIME_WINDOW

        faceTracker.entries.removeIf { (id, info) ->
            val hadOldDetections = info.recentDetections.removeIf { it < oldestAllowed }
            if (hadOldDetections) {
                info.positions.removeFirst()
            }

            val removed = info.recentDetections.isEmpty()
            if (removed) {
            }
            removed
        }
    }

    fun clear() {
        faceTracker.clear()
    }
}
