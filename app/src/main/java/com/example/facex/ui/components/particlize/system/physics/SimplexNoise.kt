package com.example.particleeffect.particlize.system.physics

import kotlin.math.cos
import kotlin.math.sin


object SimplexNoise {
    fun noise(x: Float, y: Float, z: Float): Float {
        val s = (x + y + z) * F3
        val i = fastFloor(x + s)
        val j = fastFloor(y + s)
        val k = fastFloor(z + s)

        val t = (i + j + k) * G3
        val X0 = i - t
        val Y0 = j - t
        val Z0 = k - t

        val x0 = x - X0
        val y0 = y - Y0
        val z0 = z - Z0

        return sin(x0) * cos(y0) * sin(z0) * 0.5f + 0.5f
    }

    // Added 2D noise function
    fun noise2D(x: Float, y: Float, z: Float): Pair<Float, Float> {
        return Pair(
            noise(x, y, z),
            noise(x + 1000, y + 1000, z) // Offset for different noise pattern
        )
    }

    private const val F3 = 1.0f / 3.0f
    private const val G3 = 1.0f / 6.0f

    private fun fastFloor(x: Float): Int {
        return if (x > 0) x.toInt() else x.toInt() - 1
    }
}
