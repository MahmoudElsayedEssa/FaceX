package com.example.facex.data.local.ml.detection


import android.graphics.Bitmap
import android.graphics.ImageFormat
import com.example.facex.data.local.ml.entity.SimpleFace
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import java.nio.ByteBuffer
import javax.inject.Inject

class MLKitFaceDetector @Inject constructor(
    faceDetectorOptions: FaceDetectorOptions
) : FaceDetector {
    private val detector = FaceDetection.getClient(faceDetectorOptions)

    override suspend fun detectFaces(
        bitmap: Bitmap, rotationDegrees: Int
    ): Result<List<SimpleFace>> {
        return runCatching {
            val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
            val mlKitFaces = detector.process(inputImage).await()
            mlKitFaces.map { SimpleFace(it.boundingBox, it.trackingId ?: -1) }
        }
    }

    override suspend fun detectFaces(
        buffer: ByteBuffer, width: Int, height: Int, format: Int, rotationDegrees: Int
    ): Result<List<SimpleFace>> {

        require(format == ImageFormat.NV21 || format == ImageFormat.YV12) {
            "Unsupported image format: $format"
        }

        return runCatching {
            val inputImage = InputImage.fromByteBuffer(
                buffer, width, height, rotationDegrees, format
            )
            val mlKitFaces = detector.process(inputImage).await()
            mlKitFaces.map { SimpleFace(it.boundingBox, it.trackingId ?: -1) }
        }
    }

    override suspend fun detectFaces(
        array: ByteArray, width: Int, height: Int, format: Int, rotationDegrees: Int
    ): Result<List<SimpleFace>> {

        require(format == ImageFormat.NV21 || format == ImageFormat.YV12) {
            "Unsupported image format: $format"
        }

        return runCatching {
            val inputImage = InputImage.fromByteArray(
                array, width, height, rotationDegrees, format
            )
            val mlKitFaces = detector.process(inputImage).await()
            mlKitFaces.map { SimpleFace(it.boundingBox, it.trackingId ?: -1) }
        }
    }

    override fun close() {
        detector.close()
    }
}




