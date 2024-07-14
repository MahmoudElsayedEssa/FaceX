package com.example.facex.ui.screens.camera_face_recognition.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.compose.ui.geometry.Size
import androidx.core.content.ContextCompat
import com.example.facex.R
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.RecognizedPerson
import kotlin.math.ceil

class FacesOverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var detectedFaces: List<DetectedFace> = emptyList()
    private var recognizedPersons: List<RecognizedPerson> = emptyList()

    private val detectedBoxPaint = Paint().apply {
        color = ContextCompat.getColor(context!!, R.color.purple_200)
        strokeWidth = 8F
        style = Paint.Style.STROKE
    }
    private val recognizedBoxPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 8F
        style = Paint.Style.STROKE
    }
    private val textBackgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 50f
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = 50f
    }

    private val bounds = Rect()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val imageSize = Size(IMAGE_WIDTH, IMAGE_HEIGHT)
        val viewSize = Size(width.toFloat(), height.toFloat())
        val orientation = resources.configuration.orientation

        detectedFaces.forEach { face ->
            val boundingBox = face.boundingBox
            val adjustedBox = adjustBoundingBoxForView(boundingBox, orientation, imageSize, viewSize)
            canvas.drawRect(adjustedBox, detectedBoxPaint)
        }

        recognizedPersons.forEach { person ->
            val boundingBox = person.detectedFace.boundingBox
            val adjustedBox = adjustBoundingBoxForView(boundingBox, orientation, imageSize, viewSize)

            // Draw bounding box
            canvas.drawRect(adjustedBox, recognizedBoxPaint)

            // Calculate text background and draw
            val name = person.person.name
            textPaint.getTextBounds(name, 0, name.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                adjustedBox.left,
                adjustedBox.top,
                adjustedBox.left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                adjustedBox.top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )
            canvas.drawText(
                name,
                adjustedBox.left,
                adjustedBox.top + textHeight,
                textPaint
            )
        }
    }

    private fun adjustBoundingBoxForView(
        rect: Rect,
        orientation: Int,
        imageSize: Size,
        viewSize: Size,
        cameraSelector: Int = CameraSelector.LENS_FACING_BACK,
    ): RectF {
        val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
        val isFrontMode = cameraSelector == CameraSelector.LENS_FACING_FRONT

        val (adjustedWidth, adjustedHeight) = if (isLandscape) {
            Pair(imageSize.width, imageSize.height)
        } else {
            Pair(imageSize.height, imageSize.width)
        }

        val scaleX = viewSize.width / adjustedWidth
        val scaleY = viewSize.height / adjustedHeight

        val scale = maxOf(scaleX, scaleY)

        val offsetX = (viewSize.width - ceil(adjustedWidth * scale)) / 2.0f
        val offsetY = (viewSize.height - ceil(adjustedHeight * scale)) / 2.0f

        val mappedBox = RectF(
            (rect.left.toFloat() * scale) + offsetX,
            (rect.top.toFloat() * scale) + offsetY,
            (rect.right.toFloat() * scale) + offsetX,
            (rect.bottom.toFloat() * scale) + offsetY
        )

        if (isFrontMode) {
            val centerX = viewSize.width / 2f
            mappedBox.apply {
                left = centerX + (centerX - left)
                right = centerX - (right - centerX)
            }
        }

        return mappedBox
    }

    fun setResults(detectedFaces: List<DetectedFace>, recognizedPersons: List<RecognizedPerson>) {
        this.detectedFaces = detectedFaces
        this.recognizedPersons = recognizedPersons
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
        private const val IMAGE_WIDTH = 1280f
        private const val IMAGE_HEIGHT = 720f
    }
}
