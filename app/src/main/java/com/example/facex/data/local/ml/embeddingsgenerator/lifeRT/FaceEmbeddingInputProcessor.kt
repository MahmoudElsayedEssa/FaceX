package com.example.facex.data.local.ml.embeddingsgenerator.lifeRT

import com.example.facex.data.local.ml.liteRT.InputProcessor
import com.example.facex.data.local.ml.liteRT.modelhandling.ModelInputConfig
import com.example.facex.data.toByteArray
import com.example.facex.domain.entities.Frame
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceEmbeddingInputProcessor(
    private val inputConfig: ModelInputConfig
) : InputProcessor<Frame> {

    override fun process(input: Frame): ByteBuffer {

        val byteArray = input.buffer.toByteArray()

        val mat = Mat(input.height, input.width, CvType.CV_8UC4).apply {
            put(0, 0, byteArray)
        }

        val resizedMat = Mat()
        Imgproc.resize(
            mat,
            resizedMat,
            Size(inputConfig.inputWidth.toDouble(), inputConfig.inputHeight.toDouble())
        )

        val grayMat = Mat()
        Imgproc.cvtColor(resizedMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

        val rgbMat = Mat()
        Imgproc.cvtColor(grayMat, rgbMat, Imgproc.COLOR_GRAY2RGB)

        // Prepare the output buffer
        val outputBuffer = ByteBuffer.allocateDirect(
            inputConfig.inputWidth * inputConfig.inputHeight * 3 * 4
        ).apply {
            order(ByteOrder.nativeOrder())
        }

        // Extract and normalize RGB values
        val rgbData = ByteArray(inputConfig.inputWidth * inputConfig.inputHeight * 3)
        rgbMat.get(0, 0, rgbData)

        val imageMean = inputConfig.imageMean
        val imageStd = inputConfig.imageStd

        for (i in rgbData.indices step 3) {
            val r = rgbData[i].toInt() and 0xFF
            val g = rgbData[i + 1].toInt() and 0xFF
            val b = rgbData[i + 2].toInt() and 0xFF

            outputBuffer.putFloat((r - imageMean) / imageStd)
            outputBuffer.putFloat((g - imageMean) / imageStd)
            outputBuffer.putFloat((b - imageMean) / imageStd)
        }

        return outputBuffer.apply { rewind() }
    }
}


