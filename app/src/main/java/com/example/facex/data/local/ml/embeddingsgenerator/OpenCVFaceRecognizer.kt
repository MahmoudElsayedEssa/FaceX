package com.example.facex.data.local.ml.embeddingsgenerator

import com.example.facex.data.toByteArray
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.entities.RecognitionConfig
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.logger.logWarning
import com.example.facex.domain.ml.EmbeddingGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.dnn.Dnn.DNN_BACKEND_OPENCV
import org.opencv.dnn.Dnn.DNN_TARGET_CPU
import org.opencv.dnn.Dnn.DNN_TARGET_OPENCL
import org.opencv.dnn.Dnn.DNN_TARGET_OPENCL_FP16
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.FaceRecognizerSF
import java.io.File
import java.io.InputStream

class OpenCVFaceRecognizer(
    private val modelInputStream: InputStream,
    private val logger: Logger,
    private val config: RecognitionConfig.OpenCV = RecognitionConfig.OpenCV()
) : EmbeddingGenerator {
    private val faceRecognizer: FaceRecognizerSF = tryCreateWithAvailableBackend()

    override suspend fun generateEmbedding(input: Frame): Result<FloatArray> =
        withContext(Dispatchers.Default) {
            runCatching {
                val mat = Mat(input.height, input.width, CvType.CV_8UC4).apply {
                    put(0, 0, input.buffer.toByteArray())
                }

                // Resize and preprocess the image
                val preprocessedMat = preprocessImage(mat)
                val faceFeature = Mat()

                try {
                    faceRecognizer.feature(preprocessedMat, faceFeature)

                    // Convert feature Mat to float array
                    val embedding = FloatArray(faceFeature.total().toInt()).apply {
                        faceFeature.get(0, 0, this)
                    }
                    embedding
                } finally {
                    mat.release()
                    preprocessedMat.release()
                    faceFeature.release()
                }
            }
        }

    private fun preprocessImage(mat: Mat): Mat {
        val resizedMat = Mat()
        Imgproc.resize(mat, resizedMat, config.imageSize)

        val grayMat = Mat()
        Imgproc.cvtColor(resizedMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

        val bgrMat = Mat()
        Imgproc.cvtColor(grayMat, bgrMat, Imgproc.COLOR_GRAY2BGR)

        resizedMat.release()
        grayMat.release()
        return bgrMat
    }

    private fun tryCreateWithAvailableBackend(): FaceRecognizerSF {
        val modelFile: File = File.createTempFile("face_recognition_sface_2021dec", ".onnx").apply {
            deleteOnExit()
            outputStream().use { modelInputStream.copyTo(it) }
        }

        return try {
            // Try OpenCL/GPU first
            "Attempting GPU acceleration".logInfo(logger)
            FaceRecognizerSF.create(
                modelFile.absolutePath,
                "",
                DNN_BACKEND_OPENCV,
                DNN_TARGET_OPENCL_FP16  // Try FP16 for better performance
            ).also {
                "Successfully initialized GPU backend".logInfo(logger)
            }
        } catch (e: Exception) {
            "GPU initialization failed: ${e.message}".logWarning(logger)

            try {
                // Try OpenCL without FP16
                FaceRecognizerSF.create(
                    modelFile.absolutePath, "", DNN_BACKEND_OPENCV, DNN_TARGET_OPENCL
                ).also {
                    "Successfully initialized OpenCL backend".logInfo(logger)
                }
            } catch (e: Exception) {
                "OpenCL initialization failed: ${e.message}".logWarning(logger)

                // Fallback to CPU with NEON
                FaceRecognizerSF.create(
                    modelFile.absolutePath,
                    "",
                    DNN_BACKEND_OPENCV,
                    DNN_TARGET_CPU
                ).also {
                    "Falling back to CPU (NEON) backend".logInfo(logger)
                }
            }
        }
    }

    override suspend fun close(): Unit = withContext(Dispatchers.IO) {}
}