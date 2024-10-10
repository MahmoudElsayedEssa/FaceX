package com.example.facex.ui.helpers.processors.formats.nv21


//object NV21Processor : FormatProcessorStrategy {
//    override suspend fun processImage(image: ImageProxy, outputType: ImageOutputType): ImageInput {
//        require(image.format == ImageFormat.NV21) {
//            "Unsupported format for NV21Processor: ${image.format}"
//        }
//
//        return when (outputType) {
//            ImageOutputType.Bitmap -> toBitmap(image)
//            ImageOutputType.ByteArray -> toByteArray(image)
//            ImageOutputType.ByteBuffer -> toByteBuffer(image)
//        }
//    }
//
//    private suspend fun toBitmap(image: ImageProxy): ImageInput.FromBitmap {
//        val bitmap = image.toBitmap()
//        return ImageInput.FromBitmap(bitmap)
//    }
//
//    private suspend fun toByteArray(image: ImageProxy): ImageInput.FromByteArray {
//        val nv21Bytes = imageToByteArray(image)
//        return ImageInput.FromByteArray(nv21Bytes, image.width, image.height, image.format)
//    }
//
//    private suspend fun toByteBuffer(image: ImageProxy): ImageInput.FromByteBuffer {
//        val width = image.width
//        val height = image.height
//
//        // Allocate a ByteBuffer for the NV21 format
//        val buffer = ByteBuffer.allocateDirect(width * height + (width * height / 2))
//
//        // Get the Y plane
//        val yPlane = image.planes[0].buffer
//        buffer.put(yPlane)
//
//        // Get the U and V planes
//        val uPlane = image.planes[1].buffer
//        val vPlane = image.planes[2].buffer
//
//        // Allocate space for interleaved UV data
//        val uvSize = width * height / 2
//        val uvBuffer = ByteBuffer.allocateDirect(uvSize)
//        val avgUvSize = uvSize / 2
//        // Interleave U and V values
//        for (i in 0 until avgUvSize) {
//            uvBuffer.put(uPlane.get(i))
//            uvBuffer.put(vPlane.get(i))
//        }
//
//        // Combine Y and UV buffers into the final NV21 buffer
//        buffer.put(uvBuffer.array())
//
//        // Rewind the buffer to read from the start
//        buffer.rewind()
//        return ImageInput.FromByteBuffer(buffer, image.width, image.height, image.format)
//    }
//
//    private suspend fun imageToByteArray(image: ImageProxy): ByteArray {
//        val buffer = image.planes[0].buffer
//        val bytes = ByteArray(buffer.capacity())
//        buffer.get(bytes)
//        return bytes
//    }
//}