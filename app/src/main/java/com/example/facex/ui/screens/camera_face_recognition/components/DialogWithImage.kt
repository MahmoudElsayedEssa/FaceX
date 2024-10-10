package com.example.facex.ui.screens.camera_face_recognition.components

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.facex.ui.TrackedFace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

@Composable
fun DialogWithImage(
    onDismissRequest: () -> Unit,
    onConfirmation: (name: String) -> Unit,
    trackedFace: TrackedFace,
) {

    var name by remember { mutableStateOf("") }
    var faceByteBuffer by remember { mutableStateOf<ByteBuffer?>(null) }

    LaunchedEffect(Unit) {
        faceByteBuffer = trackedFace.imageByteBuffer
    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp),
                ) {
                    faceByteBuffer?.let {
                        AsyncByteBufferImage(
                            it,
                            trackedFace.boundingBox.width(),
                            trackedFace.boundingBox.height()
                        )
                    }

                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier

                            .padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            onConfirmation(name)
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

private fun yuvToBitmap(yuvBytes: ByteArray, width: Int, height: Int): Bitmap {
    val argbBytes = IntArray(width * height)
    var index = 0

    // Convert YUV to RGB
    for (j in 0 until height) {
        for (i in 0 until width) {
            val y = yuvBytes[index].toInt() and 0xff

            // For simplicity, converting Y to grayscale. Adjust the RGB calculation as needed.
            val r = y
            val g = y
            val b = y

            argbBytes[index] = Color.argb(255, r, g, b)
            index++
        }
    }

    // Create Bitmap from the ARGB array
    val bitmap = Bitmap.createBitmap(argbBytes, width, height, Bitmap.Config.ARGB_8888)

    // Rotate the bitmap if necessary
    return rotateBitmap(bitmap, 90F) // Rotate by 90 degrees if needed
}

private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(angle)
    }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}


suspend fun byteBufferToBitmapAsync(buffer: ByteBuffer, width: Int, height: Int): Bitmap? {
    return withContext(Dispatchers.Default) {
        try {
            // Adjust expected size for cropped NV21 format or adjust based on format
            val expectedSize = width * height   // Adjust this if cropped
            if (buffer.remaining() != expectedSize) {
                val message =
                    "Buffer size (${buffer.remaining()}) does not match the expected size for NV21 format ($expectedSize) with dimensions ${width}x$height}"
                Log.d("MAMO", "byteBufferToBitmapAsync: $message")
                throw IllegalArgumentException(message)
            }

            val nv21Bytes = ByteArray(buffer.remaining())
            buffer.get(nv21Bytes)

            // If necessary, convert to NV21 from another format here
            // Example: Convert from YUV_420_888 to NV21
            yuvToBitmap(nv21Bytes, width, height)
        } catch (e: Exception) {
            Log.d("MAMO", "byteBufferToBitmapAsync: ${e.message}")
            println("Error converting ByteBuffer to Bitmap: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}


@Composable
fun AsyncByteBufferImage(
    buffer: ByteBuffer,
    width: Int,
    height: Int,
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(buffer) {
        scope.launch {
            try {
                bitmap = byteBufferToBitmapAsync(buffer, width, height)
                if (bitmap == null) {
                    errorMessage = "Failed to convert ByteBuffer to Bitmap"
                }
            } catch (e: IllegalArgumentException) {
                errorMessage = e.message
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Rendered YUV Image",
            modifier = Modifier.fillMaxSize() // Fill the available space
        )
    } ?: run {
        Text(text = "No image available")
    }
}