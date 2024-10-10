package com.example.facex.ui.helpers.models

import android.graphics.ImageFormat
import com.example.facex.domain.entities.toImageFormat

sealed class ImageOutputType(open val format: Int) {
    data class Bitmap(override val format: Int = android.graphics.Bitmap.Config.ARGB_8888.toImageFormat()) :
        ImageOutputType(format) {
        init {
            require(
                format == android.graphics.Bitmap.Config.ARGB_8888.toImageFormat() || format == android.graphics.Bitmap.Config.RGB_565.toImageFormat()
            ) {
                "Unsupported format: $format for a bitmap, try ARGB_8888 or RGB_565"
            }
        }

    }

    data class ByteArray(override val format: Int = ImageFormat.YUV_420_888) :
        ImageOutputType(format)

    data class ByteBuffer(override val format: Int = ImageFormat.YUV_420_888) :
        ImageOutputType(format)
}