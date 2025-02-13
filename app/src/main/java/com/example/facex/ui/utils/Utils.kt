package com.example.facex.ui.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.entities.Ratio
import com.example.facex.ui.screens.camera_face_recognition.BoundingBox
import com.example.facex.ui.screens.camera_face_recognition.FrameData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.nio.ByteBuffer
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toKotlinDuration

fun Frame.toBitmap(): Bitmap {
    val buffer = this.buffer.duplicate().apply { rewind() }
    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    bitmap.copyPixelsFromBuffer(buffer)
    return bitmap
}



fun Bitmap.cropFace(boundingBox: Rect): Bitmap? {
    return try {
        val x = boundingBox.left.coerceIn(0, this.width)
        val y = boundingBox.top.coerceIn(0, this.height)
        val width = (boundingBox.right - boundingBox.left).coerceIn(0, this.width - x)
        val height = (boundingBox.bottom - boundingBox.top).coerceIn(0, this.height - y)

        Bitmap.createBitmap(
            this, x, y, width, height
        )
    } catch (e: Exception) {
        null
    }
}

fun Bitmap.cropFace(boundingBox: BoundingBox): Bitmap? {
    return try {
        val x = boundingBox.left.toInt().coerceIn(0, this.width)
        val y = boundingBox.top.toInt().coerceIn(0, this.height)
        val width = (boundingBox.right - boundingBox.left).toInt().coerceIn(0, this.width - x)
        val height = (boundingBox.bottom - boundingBox.top).toInt().coerceIn(0, this.height - y)

        Bitmap.createBitmap(
            this, x, y, width, height
        )
    } catch (e: Exception) {
        null
    }
}

fun Bitmap.rotateBitmap(rotation: Int): Bitmap {
    if (rotation == 0) return this

    val matrix = Matrix().apply {
        postRotate(rotation.toFloat())
    }

    return try {
        Bitmap.createBitmap(
            this, 0, 0, this.width, this.height, matrix, true
        ).also {
            if (it != this) {
                this.recycle()
            }
        }
    } catch (e: OutOfMemoryError) {
        this
    }
}


fun Duration.formatToTwoDigits(): String =
    toString().replace("([\\d.]+)([a-z]+)".toRegex()) { matchResult ->
        val value = matchResult.groupValues[1].toDouble()
        val unit = matchResult.groupValues[2]
        "%.2f%s".format(value, unit)
    }


fun Instant.formatToTwoDigits(): String {
    val duration = java.time.Duration.between(this, Instant.now()).toKotlinDuration()

//   return this.nano.toDuration(DurationUnit.NANOSECONDS).formatDetailed()
    return duration.formatDetailed()
}


fun Duration.formatDetailed(): String {
    val nanos = this.inWholeNanoseconds

    return when {
        nanos < 1_000_000 -> "${nanos / 1_000}µs"
        nanos < 1_000_000_000 -> "${nanos / 1_000_000}ms".let { if (nanos % 1_000_000 > 0) "$it ${(nanos % 1_000_000) / 1_000}µs" else it }
        nanos < 60_000_000_000 -> "${nanos / 1_000_000_000}s".let { if (nanos % 1_000_000_000 > 0) "$it ${(nanos % 1_000_000_000) / 1_000_000}ms" else it }
        nanos < 3_600_000_000_000 -> buildString {
            append("${nanos / 60_000_000_000}m")
            (nanos % 60_000_000_000 / 1_000_000_000).takeIf { it > 0 }?.let { append(" ${it}s") }
            (nanos % 1_000_000_000 / 1_000_000).takeIf { it > 0 }?.let { append(" ${it}ms") }
        }

        else -> buildString {
            append("${nanos / 3_600_000_000_000}h")
            (nanos % 3_600_000_000_000 / 60_000_000_000).takeIf { it > 0 }
                ?.let { append(" ${it}m") }
            (nanos % 60_000_000_000 / 1_000_000_000).takeIf { it > 0 }?.let { append(" ${it}s") }
        }
    }
}

object TimeFormatter {
    fun Long.formatTimeValue(): String {
        return when {
            this < 1000 -> "${this}ms"
            this < 60000 -> {
                val seconds = this / 1000.0
                "%.1fs".format(seconds)
            }

            else -> {
                val minutes = this / 60000.0
                "%.2fmin".format(minutes)
            }
        }
    }
}

fun Long.formatTimeValue(): String {
    return when {
        this >= 3600000 -> { // >= 1 hour
            val hours = this / 3600000
            val minutes = (this % 3600000) / 60000
            val seconds = (this % 60000) / 1000
            "%d:%02d:%02d".format(hours, minutes, seconds)
        }

        this >= 60000 -> { // >= 1 minute
            val minutes = this / 60000
            val seconds = (this % 60000) / 1000
            "%dm:%02ds".format(minutes, seconds)
        }

        this >= 10000 -> { // >= 10 seconds
            val seconds = this / 1000f
            "%.1fs".format(seconds)
        }

        this >= 1000 -> { // >= 1 second
            val seconds = this / 1000f
            "%.2fs".format(seconds)
        }

        else -> { // < 1 second
            "${this}ms"
        }
    }
}

fun Instant.formatDetailed(): String =
    java.time.Duration.between(this, Instant.now()).abs().toKotlinDuration().formatDetailed()

fun Duration.calculateFPS(): Double {
    return 1.toDuration(DurationUnit.SECONDS) / this
}

fun <T> Flow<T>.throttle(periodMillis: Long): Flow<T> = flow {
    var lastEmissionTime = 0L
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmissionTime >= periodMillis) {
            lastEmissionTime = currentTime
            emit(value)
        }
    }
}.distinctUntilChanged()


fun String.splitToWords() = replace(Regex("(?<=.)(?=\\p{Upper})"), " ")


object CameraAspectRatios {
    val RATIO_4_3: Ratio = 4f to 3f
    val RATIO_3_4: Ratio = 3f to 4f
    val RATIO_16_9: Ratio = 16f to 9f
    val RATIO_9_16: Ratio = 9f to 16f
}
