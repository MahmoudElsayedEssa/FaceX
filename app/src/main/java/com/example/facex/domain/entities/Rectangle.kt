package com.example.facex.domain.entities

data class Rectangle(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    init {
        require(width >= 0) { "Width must be non-negative" }
        require(height >= 0) { "Height must be non-negative" }
    }

    val isEmpty: Boolean get() = width == 0 || height == 0
}
