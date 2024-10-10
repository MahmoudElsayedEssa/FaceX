package com.example.facex.data.local.ml.embeddings_generator

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.face.LBPHFaceRecognizer
import org.opencv.imgproc.Imgproc

class OpenCVFaceRecognizer {

    private val recognizer: LBPHFaceRecognizer = LBPHFaceRecognizer.create()

    // Train the recognizer with a dataset of labeled face images
    fun trainRecognizer(faceImages: List<Bitmap>, labels: List<Int>) {
        val mats = faceImages.map { BitmapToMat(it, Imgproc.COLOR_BGR2GRAY) }
        val labelMat = MatOfInt(*labels.toIntArray())
        recognizer.train(mats, labelMat)
    }

    // Recognize a face from a given bitmap
    fun recognizeFace(faceBitmap: Bitmap): Int {
        val mat = BitmapToMat(faceBitmap, Imgproc.COLOR_BGR2GRAY)
        val label = IntArray(1)
        val confidence = DoubleArray(1)
        recognizer.predict(mat, label, confidence)

        // Return the label with the lowest confidence
        return label[0]
    }

    // Save the trained model
    fun saveModel(filePath: String) {
        recognizer.save(filePath)
    }

    // Load a saved model
    fun loadModel(filePath: String) {
        recognizer.read(filePath)
    }

    private fun BitmapToMat(bitmap: Bitmap, colorConversionCode: Int): Mat {
        val mat = Mat()
        val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bmp32, mat)
        Imgproc.cvtColor(mat, mat, colorConversionCode)
        return mat
    }

    fun close() {
//        recognizer.free()
    }
}
