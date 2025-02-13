package com.example.facex.data.local.ml.detection

import android.graphics.Rect
import com.example.facex.data.toByteArray
import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.ml.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.FaceDetectorYN
import java.io.InputStream


class OpenCVFaceDetector(
    modelInputStream: InputStream, private val config: DetectionConfig.OpenCV
) : FaceDetector {
    private var faceDetector: FaceDetectorYN? = createDetector(modelInputStream)

    private fun createDetector(inputStream: InputStream): FaceDetectorYN {
        val modelBuffer = MatOfByte(*inputStream.readBytes())
        val configBuffer = MatOfByte()

        return FaceDetectorYN.create(
            "onnx",
            modelBuffer,
            configBuffer,
            config.imageSize,
            config.scoreThreshold,
            config.nmsThreshold,
            config.topK,
        ).apply {
            modelBuffer.release()
            configBuffer.release()
        }
    }


    override suspend fun detectFaces(image: Frame): Result<List<FaceDetectionResult>> =
        withContext(Dispatchers.Default) {
            try {
                // Convert ImageData to OpenCV Mat in its original orientation
                val rgbaMat = Mat(image.height, image.width, CvType.CV_8UC4).apply {
                    put(0, 0, image.buffer.toByteArray())
                }

                // Calculate the actual dimensions after rotation
                val (rotatedWidth, rotatedHeight) = when (image.rotationDegrees) {
                    90, 270 -> Pair(image.height, image.width)
                    else -> Pair(image.width, image.height)
                }

                // Apply rotation to match device orientation
                val rotatedMat = when (image.rotationDegrees) {
                    90 -> Mat().also { dest ->
                        Core.rotate(rgbaMat, dest, Core.ROTATE_90_CLOCKWISE)
                        rgbaMat.release()
                    }

                    180 -> Mat().also { dest ->
                        Core.rotate(rgbaMat, dest, Core.ROTATE_180)
                        rgbaMat.release()
                    }

                    270 -> Mat().also { dest ->
                        Core.rotate(rgbaMat, dest, Core.ROTATE_90_COUNTERCLOCKWISE)
                        rgbaMat.release()
                    }

                    else -> rgbaMat
                }

                // Process image through OpenCV pipeline
                val bgrMat = Mat().also {
                    Imgproc.cvtColor(rotatedMat, it, Imgproc.COLOR_RGBA2BGR)
                    rotatedMat.release()
                }

                val resizedMat = Mat().also {
                    Imgproc.resize(bgrMat, it, config.imageSize)
                    bgrMat.release()
                }

                val faces = Mat()
                faceDetector?.detect(resizedMat, faces)
                resizedMat.release()

                // Calculate scaling factors using rotated dimensions
                val scaleX = rotatedWidth.toDouble() / config.imageSize.width
                val scaleY = rotatedHeight.toDouble() / config.imageSize.height

                // Transform coordinates back to display orientation
                Result.success(faces.toDetectionResults(scaleX, scaleY)).also { faces.release() }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun Mat.toDetectionResults(
        scaleX: Double,
        scaleY: Double,
    ): List<FaceDetectionResult> = (0 until rows()).map { row ->
        FloatArray(cols()).let { data ->
            get(row, 0, data)
            val boundingBox = Rect(
                (data[0] * scaleX).toInt(),
                (data[1] * scaleY).toInt(),
                ((data[0] + data[2]) * scaleX).toInt(),
                ((data[1] + data[3]) * scaleY).toInt()
            )


            FaceDetectionResult(boundingBox, trackingId = row)
        }
    }

    override suspend fun close() {
        faceDetector = null
    }

}