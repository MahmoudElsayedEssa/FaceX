package com.example.facex.ui.helpers.processors.conversion

import android.graphics.ImageFormat.NV21
import android.graphics.ImageFormat.UNKNOWN
import android.graphics.ImageFormat.YUV_420_888
import com.example.facex.ui.helpers.processors.formats.yuv420888.converters.Yuv420888ToBitmapConverter
import com.example.facex.ui.helpers.processors.formats.yuv420888.converters.Yuv420888ToNv21Converter

object ImageFormatConversionFactory {
    fun getConversionStrategy(inputFormat: Int, outputFormat: Int): ImageFormatConversion {
        return when {
            inputFormat == YUV_420_888 && outputFormat == NV21 -> Yuv420888ToNv21Converter

            inputFormat == YUV_420_888 && outputFormat == UNKNOWN -> Yuv420888ToBitmapConverter

            else -> error("Unsupported format conversion from $inputFormat to $outputFormat")
        }
    }
}
