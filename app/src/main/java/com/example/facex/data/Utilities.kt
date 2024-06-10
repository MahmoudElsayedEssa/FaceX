package com.example.facex.data

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toByteBuffer(): ByteBuffer {
    return ByteBuffer.wrap(this).order(ByteOrder.nativeOrder())
}

fun ByteBuffer.toByteArray(): ByteArray {
    val byteBuffer = this.duplicate().order(ByteOrder.nativeOrder())
    val byteArray = ByteArray(byteBuffer.remaining())
    byteBuffer.get(byteArray)
    return byteArray
}
