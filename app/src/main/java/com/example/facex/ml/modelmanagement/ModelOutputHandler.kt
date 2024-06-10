package com.example.facex.ml.modelmanagement

import java.nio.ByteBuffer


class TFLiteOutputHandler(outputSize: Int) {
    val outputBuffer: ByteBuffer = ByteBuffer.allocateDirect(outputSize)

    val outputData: FloatArray
        get() {
            val outputData = FloatArray(outputBuffer.capacity() / 4)
            outputBuffer.rewind()
            for (i in outputData.indices) {
                outputData[i] = outputBuffer.getFloat()
            }
            return outputData
        }
}
