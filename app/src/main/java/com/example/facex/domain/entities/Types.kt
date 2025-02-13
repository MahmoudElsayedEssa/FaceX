package com.example.facex.domain.entities

typealias Ratio = Pair<Float, Float>

typealias Timestamp = Long
typealias Value = Number


fun Ratio.aspectRatio(): Float = first / second

fun Ratio.scale(factor: Float): Ratio = first * factor to second * factor

fun Ratio.normalize(): Ratio {
    val max = maxOf(first, second)
    return first / max to second / max
}