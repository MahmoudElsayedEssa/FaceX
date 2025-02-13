package com.example.facex.data.local.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object Converters {
    @TypeConverter
    fun fromByteBuffer(byteBuffer: ByteBuffer): ByteArray {
        byteBuffer.rewind()
        val byteArray = ByteArray(byteBuffer.remaining())
        byteBuffer.get(byteArray)
        return byteArray
    }

    @TypeConverter
    fun toByteBuffer(byteArray: ByteArray): ByteBuffer = ByteBuffer.wrap(byteArray)

    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? = value?.joinToString(",")

    @TypeConverter
    fun toFloatArray(value: String?): FloatArray? = value?.split(",")?.map { it.toFloat() }?.toFloatArray()

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
