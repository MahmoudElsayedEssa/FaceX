package com.example.facex.data.local.ml.embeddingsgenerator.lifeRT

import com.example.facex.data.local.ml.liteRT.OutputProcessor
import com.example.facex.data.local.ml.liteRT.modelhandling.DataType
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelOutputConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder


class EmbeddingOutputProcessor(
    private val config: ModelOutputConfig,
) : OutputProcessor<FloatArray> {

    private val floatBuffer = FloatArray(config.embeddingSize)
    private val byteBuffer = ByteArray(config.embeddingSize)

    private val outputBuffer: ByteBuffer by lazy {
        ByteBuffer.allocateDirect(config.embeddingSize * config.dataType.byteSize)
            .apply { order(ByteOrder.nativeOrder()) }
    }

    override fun createOutput(): ByteBuffer = outputBuffer.apply { clear() }

    override fun process(buffer: ByteBuffer): FloatArray {
        buffer.rewind()
        return when (config.dataType) {
            DataType.FLOAT32 -> processFloat(buffer)
            DataType.INT8, DataType.UINT8 -> processQuantized(buffer)
        }
    }

    private fun processFloat(buffer: ByteBuffer): FloatArray {
        buffer.rewind()
        if (buffer.remaining() / config.dataType.byteSize != floatBuffer.size) {
            throw IllegalStateException(
                "Buffer size mismatch: expected ${floatBuffer.size}, found ${buffer.remaining() / Float.SIZE_BYTES}"
            )
        }
        buffer.asFloatBuffer().get(floatBuffer)
        return floatBuffer
    }


    private fun processQuantized(buffer: ByteBuffer): FloatArray {
        buffer.get(byteBuffer)

        // Process in chunks for better vectorization
        val chunkSize = 64
        var offset = 0
        while (offset < byteBuffer.size) {
            val end = minOf(offset + chunkSize, byteBuffer.size)
            for (i in offset until end) {
                floatBuffer[i] = (byteBuffer[i] - config.zeroPoint) * config.scale
            }
            offset += chunkSize
        }

        return floatBuffer
    }
}