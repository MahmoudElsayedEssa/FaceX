package com.example.facex.data.local.ml.detection

//class TFFaceDetector(
//    private val modelFile: String,
//    private val delegateHandler: TFLiteDelegateHandler
//) : FaceDetector {
//    private var interpreter: Interpreter? = null
//
//    init {
//        val options = delegateHandler.createInterpreterOptions(DelegateType.CPU) // Or GPU/NNAPI
//        interpreter = Interpreter(FileUtil.loadMappedFile(context, modelFile), options)
//    }
//
//    @WorkerThread
//    override suspend fun detectFaces(bitmap: Bitmap, rotationDegrees: Int): List<SimpleFace> {
//        return withContext(Dispatchers.IO) {
//            val input = TensorImage.fromBitmap(bitmap)
//            val output = Array(1) { Array(1) { FloatArray(4) } }  // Example output for bounding boxes
//            interpreter?.run(input.buffer, output)
//
//            output.map {
//                SimpleFace(trackedId = it.hashCode(), boundingBox = RectF(it[0][0], it[0][1], it[0][2], it[0][3]))
//            }
//        }
//    }
//
//    override suspend fun detectFaces(
//        processedImage: ProcessedImage,
//        rotationDegrees: Int
//    ): List<SimpleFace> {
//        TODO("Not yet implemented")
//    }
//
//    override fun close() {
//        interpreter?.close()
//    }
//}
