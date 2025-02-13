package com.example.facex.data.local.ml.detection

import android.graphics.ImageFormat
import com.example.facex.domain.entities.FaceDetectionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.ml.FaceDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock


private class MLKitByteArrayPool(private val maxSize: Int = 3) {
    private val lock = ReentrantLock()
    private val pools = mutableMapOf<Int, ArrayDeque<ByteArray>>()

    fun acquire(size: Int): ByteArray = lock.withLock {
        val pool = pools.getOrPut(size) { ArrayDeque(maxSize) }
        return pool.removeFirstOrNull() ?: ByteArray(size)
    }

    fun release(array: ByteArray) = lock.withLock {
        val size = array.size
        val pool = pools.getOrPut(size) { ArrayDeque(maxSize) }
        if (pool.size < maxSize) {
            pool.addLast(array)
        }
    }

    fun cleanup() = lock.withLock {
        pools.clear()
    }
}

class MLKitFaceDetector @Inject constructor(
    private val faceDetectorOptions: FaceDetectorOptions,
    private val logger: Logger
) : FaceDetector {
    private val byteArrayPool = MLKitByteArrayPool()

    private val detector by lazy { FaceDetection.getClient(faceDetectorOptions) }

    override suspend fun detectFaces(image: Frame): Result<List<FaceDetectionResult>> {
        // Acquire arrays from pool
        val rgbaArray = byteArrayPool.acquire(image.width * image.height * 4)
        val yv12Array = byteArrayPool.acquire(image.width * image.height * 3 / 2)

        return try {
            // Use the arrays
            image.buffer.position(0)
            image.buffer.get(rgbaArray)
            image.buffer.position(0)

            val mat = Mat(image.height, image.width, CvType.CV_8UC4).apply {
                put(0, 0, rgbaArray)
            }
            val nv21Mat = Mat()
            Imgproc.cvtColor(mat, nv21Mat, Imgproc.COLOR_RGBA2YUV_YV12)

            nv21Mat.get(0, 0, yv12Array)

            mat.release()
            nv21Mat.release()

            val inputImage = InputImage.fromByteArray(
                yv12Array,
                image.width,
                image.height,
                image.rotationDegrees,
                ImageFormat.YV12
            )

            runCatching {
                detector.process(inputImage).await().map { face ->
                    FaceDetectionResult(
                        boundingBox = face.boundingBox,
                        trackingId = face.trackingId ?: -1
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            // Return arrays to pool
            byteArrayPool.release(rgbaArray)
            byteArrayPool.release(yv12Array)
        }
    }


    override suspend fun close() {
        detector.close()
        byteArrayPool.cleanup()
    }
}

