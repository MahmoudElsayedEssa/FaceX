package com.example.facex.data.local.ml.liteRT

import java.nio.ByteBuffer

interface InputProcessor<in T> {
    fun process(input: T): ByteBuffer
}
