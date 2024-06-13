package com.example.facex.ui.screens.camera_face_recognition.components

import androidx.compose.ui.graphics.Color

//@Composable
//fun DrawFacesBoundingBoxes(detectedFaces: List<RecognizedFace>) {
//    val context = LocalContext.current
//    val orientation = context.resources.configuration.orientation
//
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        detectedFaces.forEach { detectedFace ->
//
//            val faceDimensions = adjustBoundingBoxForView(
//                rect = detectedFace.boundingBox,
//                imageSize = detectedFace.imageSize,
//                orientation = orientation,
//                viewSize = this.size,
//            )
//
//            val colorID =
//                if (detectedFace.trackingId == null) 0 else abs(detectedFace.trackingId % NUM_COLORS)
//
//            drawRoundRect(
//                color = COLORS[colorID],
//                topLeft = Offset(faceDimensions.left, faceDimensions.top),
//                size = Size(faceDimensions.width(), faceDimensions.height()),
//                style = Stroke(width = 4f),
//                cornerRadius = CornerRadius(20f, 20f)
//            )
//
//
//            if (detectedFace.isRecognized) {
//                val textPaint = Paint().asFrameworkPaint().apply {
//                    color = android.graphics.Color.WHITE
//                    textSize = 30f
//                    isAntiAlias = true
//                }
//
//                val baseText =
//                    "Name: ${detectedFace.person?.name} distance = %.2f".format(detectedFace.distance)
//                val nationalIdText = detectedFace.person?.id?.let { " National Id: $it" } ?: ""
//                val text = baseText + nationalIdText
//
//                val x = faceDimensions.right + 10
//                val y = faceDimensions.top + textPaint.textSize - 40
//
//                // Calculate the width and height of the text
//                val textWidth = textPaint.measureText(text)
//                val textHeight = textPaint.textSize
//
//                // Define the margin and background color
//                val margin = 5
//                val backgroundPaint = Paint().asFrameworkPaint().apply {
//                    color = android.graphics.Color.BLACK
//                    isAntiAlias = true
//                }
//
//                // Draw background rectangle
//                val rectLeft = x - margin
//                val rectTop = y - textHeight - margin
//                val rectRight = x + textWidth + margin
//                val rectBottom = y + margin
//                drawIntoCanvas {
//                    it.nativeCanvas.drawRect(
//                        rectLeft,
//                        rectTop,
//                        rectRight,
//                        rectBottom,
//                        backgroundPaint
//                    )
//                }
//
//                // Draw the text on top of the rectangle
//                drawIntoCanvas { it.nativeCanvas.drawText(text, x, y, textPaint) }
//            }
//
//        }
//    }
//}

const val NUM_COLORS = 10
val COLORS = arrayOf(
    Color.Black,
    Color.White,
    Color.Magenta,
    Color.LightGray,
    Color.Red,
    Color.Blue,
    Color.DarkGray,
    Color.Green,
    Color.Yellow,
    Color.Cyan
)
