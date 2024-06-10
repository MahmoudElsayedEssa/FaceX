package com.example.facex.ml.modelmanagement

import java.nio.ByteBuffer


class TFLiteInputHandler(inputSize: Int) {
    val inputBuffer: ByteBuffer = ByteBuffer.allocateDirect(inputSize)

    fun setInputData(inputData: FloatArray) {
        inputBuffer.clear()
        for (value in inputData) {
            inputBuffer.putFloat(value)
        }
        inputBuffer.rewind()
    }
}
