package com.example.facex.data.local.camera

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.imgproc.Imgproc

object OpenCVImageUtils {
    private const val THRESHOLD_SIGNIFICANT_CHANGE = 7_000_000.0
    const val SHARPNESS_THRESHOLD = 250.0
    private const val TAG = "OpenCVImageUtils"

    fun calculateSharpness(bitmap: Bitmap): Double {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY)

        val laplacianMat = Mat()
        Imgproc.Laplacian(grayMat, laplacianMat, CvType.CV_64F)

        val stddev = MatOfDouble()
        Core.meanStdDev(laplacianMat, MatOfDouble(), stddev)

        val variance = stddev.toArray()[0].let { it * it }

        listOf(mat, grayMat, laplacianMat, stddev).forEach { it.release() }

        return variance
    }

    fun isSignificantChange(currentBitmap: Bitmap, previousBitmap: Bitmap): Boolean {
        val currentMat = bitmapToMat(currentBitmap)
        val previousMat = bitmapToMat(previousBitmap)

        Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.cvtColor(previousMat, previousMat, Imgproc.COLOR_RGB2GRAY)

        val diffMat = Mat()
        Core.absdiff(currentMat, previousMat, diffMat)

        Imgproc.threshold(diffMat, diffMat, 25.0, 255.0, Imgproc.THRESH_BINARY)
        val change = Core.sumElems(diffMat).`val`[0] > THRESHOLD_SIGNIFICANT_CHANGE

        listOf(currentMat, previousMat, diffMat).forEach { it.release() }

        return change
    }

    private fun bitmapToMat(bitmap: Bitmap): Mat {
        return Mat(bitmap.height, bitmap.width, CvType.CV_8UC3).also {
            Utils.bitmapToMat(bitmap, it)
        }
    }
}