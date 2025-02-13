package com.example.facex.data.local.ml.liteRT

import java.nio.ByteBuffer

interface OutputProcessor<out R> {
    fun createOutput(): ByteBuffer
    fun process(buffer: ByteBuffer): R
}
