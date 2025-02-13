package com.example.facex.domain.entities

sealed class ImageFormat(val bytesPerPixel: Float, private val description: String) {
    override fun toString(): String = description

    object RGBA8888 : ImageFormat(4f, "RGBA8888 Format (32-bit RGBA)")
    object YUV420888 : ImageFormat(1.5f, "YUV420888 Format (YUV 4:2:0 planar)")
    object NV21 : ImageFormat(1.5f, "NV21 Format (YUV 4:2:0)")
}
