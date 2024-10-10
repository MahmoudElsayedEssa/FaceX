package com.example.facex.data.local.ml.detection

//class OpenCVFaceDetector : FaceDetector {
//    private var cascadeClassifier: CascadeClassifier? = null
//
//    init {
//        // Load OpenCV classifier (e.g., haarcascade_frontalface_alt2.xml)
//        val inputStream = context.resources.openRawResource(R.raw.haarcascade_frontalface_alt2)
//        val cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE)
//        val cascadeFile = File(cascadeDir, "haarcascade_frontalface_alt2.xml")
//        val fos = FileOutputStream(cascadeFile)
//
//        inputStream.use { input ->
//            fos.use { output ->
//                input.copyTo(output)
//            }
//        }
//
//        cascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)
//    }
//
//    @WorkerThread
//    override suspend fun detectFaces(bitmap: Bitmap, rotationDegrees: Int): List<SimpleFace> {
//        return withContext(Dispatchers.IO) {
//            // Convert bitmap to OpenCV Mat and process for face detection
//            val mat = Mat()
//            Utils.bitmapToMat(bitmap, mat)
//
//            val faces = MatOfRect()
//            cascadeClassifier?.detectMultiScale(mat, faces)
//
//            faces.toArray().map {
//                SimpleFace(trackedId = it.hashCode(), boundingBox = it)
//            }
//        }
//    }
//
//    @WorkerThread
//    override suspend fun detectFaces(
//        processedImage: ProcessedImage,
//        rotationDegrees: Int
//    ): List<SimpleFace> {
//        // Similar to bitmap processing, but using byte buffer or raw data
//        TODO("Implement detection for byte buffer images")
//    }
//
//    override fun close() {
//        cascadeClassifier?.release()
//        cascadeClassifier = null
//    }
//}
