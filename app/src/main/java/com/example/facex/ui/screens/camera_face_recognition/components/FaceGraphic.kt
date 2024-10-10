package com.example.facex.ui.screens.camera_face_recognition.components


//class FaceGraphic(
//    overlay: GraphicOverlay,
//    private val boundingBox: Rect,
//    private val trackingId: Int?
//) :
//    GraphicOverlay.Graphic(overlay) {
//    private val facePositionPaint: Paint
//    private val numColors = COLORS.size
//    private val idPaints = Array(numColors) { Paint() }
//    private val boxPaints = Array(numColors) { Paint() }
//    private val labelPaints = Array(numColors) { Paint() }
//
//    init {
//        val selectedColor = Color.WHITE
//        facePositionPaint = Paint()
//        facePositionPaint.color = selectedColor
//        for (i in 0 until numColors) {
//            idPaints[i] = Paint()
//            idPaints[i].color = COLORS[i][0]
//            idPaints[i].textSize = ID_TEXT_SIZE
//            boxPaints[i] = Paint()
//            boxPaints[i].color = COLORS[i][1]
//            boxPaints[i].style = Paint.Style.STROKE
//            boxPaints[i].strokeWidth = BOX_STROKE_WIDTH
//            labelPaints[i] = Paint()
//            labelPaints[i].color = COLORS[i][1]
//            labelPaints[i].style = Paint.Style.FILL
//        }
//    }
//    override fun draw(canvas: Canvas) {
//        // Translate and scale the bounding box coordinates
//        val left = translateX(boundingBox.left.toFloat())
//        val top = translateY(boundingBox.top.toFloat())
//        val right = translateX(boundingBox.right.toFloat())
//        val bottom = translateY(boundingBox.bottom.toFloat())
//
//        val lineHeight = scale(ID_TEXT_SIZE) + scale(BOX_STROKE_WIDTH)
//
//        var yLabelOffset: Float = if (trackingId == null) 0f else -lineHeight
//
//        // Decide color based on face ID
//        val colorID = if (trackingId == null) 0 else abs(trackingId % NUM_COLORS)
//
//        // Calculate width and height of label box
//        val textWidth = idPaints[colorID].measureText("ID: $trackingId")
//
//        yLabelOffset -= 3 * lineHeight
//
//        // Draw labels
//        canvas.drawRect(
//            left - scale(BOX_STROKE_WIDTH),
//            top + yLabelOffset,
//            left + textWidth + 2 * scale(BOX_STROKE_WIDTH),
//            top,
//            labelPaints[colorID]
//        )
//        yLabelOffset += scale(ID_TEXT_SIZE)
//        canvas.drawRect(left, top, right, bottom, boxPaints[colorID])
//        if (trackingId != null) {
//            canvas.drawText("ID: $trackingId", left, top + yLabelOffset, idPaints[colorID])
//            yLabelOffset += lineHeight
//        }
//    }
//
//    /** Draws the face annotations for position on the supplied canvas. */
////    override fun draw(canvas: Canvas) {
////        val x = translateX(boundingBox.centerX().toFloat())
////        val y = translateY(boundingBox.centerY().toFloat())
////
////        // Calculate positions.
////        val left = x - scale(boundingBox.width() / 2.0f)
////        val top = y - scale(boundingBox.height() / 2.0f)
////        val right = x + scale(boundingBox.width() / 2.0f)
////        val bottom = y + scale(boundingBox.height() / 2.0f)
////
////        val lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH
////
////        var yLabelOffset: Float = if (trackingId == null) 0f else -lineHeight
////
////        // Decide color based on face ID
////        val colorID = if (trackingId == null) 0 else abs(trackingId % NUM_COLORS)
////
////        // Calculate width and height of label box
////        val textWidth = idPaints[colorID].measureText("ID: $trackingId")
////
////        yLabelOffset -= 3 * lineHeight
////
////        // Draw labels
////        canvas.drawRect(
////            left - BOX_STROKE_WIDTH,
////            top + yLabelOffset,
////            left + textWidth + 2 * BOX_STROKE_WIDTH,
////            top,
////            labelPaints[colorID]
////        )
////        yLabelOffset += ID_TEXT_SIZE
////        canvas.drawRect(left, top, right, bottom, boxPaints[colorID])
////        if (trackingId != null) {
////            canvas.drawText("ID: $trackingId", left, top + yLabelOffset, idPaints[colorID])
////            yLabelOffset += lineHeight
////        }
////
////    }
//
//    companion object {
//        private const val ID_TEXT_SIZE = 5.0f
//        private const val BOX_STROKE_WIDTH = 5.0f
//        private const val NUM_COLORS = 10
//        private val COLORS =
//            arrayOf(
//                intArrayOf(Color.BLACK, Color.WHITE),
//                intArrayOf(Color.WHITE, Color.MAGENTA),
//                intArrayOf(Color.BLACK, Color.LTGRAY),
//                intArrayOf(Color.WHITE, Color.RED),
//                intArrayOf(Color.WHITE, Color.BLUE),
//                intArrayOf(Color.WHITE, Color.DKGRAY),
//                intArrayOf(Color.BLACK, Color.CYAN),
//                intArrayOf(Color.BLACK, Color.YELLOW),
//                intArrayOf(Color.WHITE, Color.BLACK),
//                intArrayOf(Color.BLACK, Color.GREEN)
//            )
//    }
//}


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.facex.ui.TrackedFace
import kotlin.math.abs

class FaceGraphic(
    overlay: GraphicOverlay,
    val trackedFace: TrackedFace,
) : GraphicOverlay.Graphic(overlay) {
    private val facePositionPaint: Paint
    private val numColors = COLORS.size
    private val idPaints = Array(numColors) { Paint() }
    private val boxPaints = Array(numColors) { Paint() }
    private val labelPaints = Array(numColors) { Paint() }

    init {
        val selectedColor = Color.WHITE
        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor
        for (i in 0 until numColors) {
            idPaints[i] = Paint()
            idPaints[i].color = COLORS[i][0]
            idPaints[i].textSize = ID_TEXT_SIZE
            boxPaints[i] = Paint()
            boxPaints[i].color = COLORS[i][1]
            boxPaints[i].style = Paint.Style.STROKE
            boxPaints[i].strokeWidth = BOX_STROKE_WIDTH
            labelPaints[i] = Paint()
            labelPaints[i].color = COLORS[i][1]
            labelPaints[i].style = Paint.Style.FILL
        }
    }

    fun containsPoint(x: Float, y: Float): Boolean {
        val left = translateX(trackedFace.boundingBox.left.toFloat())
        val top = translateY(trackedFace.boundingBox.top.toFloat())
        val right = translateX(trackedFace.boundingBox.right.toFloat())
        val bottom = translateY(trackedFace.boundingBox.bottom.toFloat())

        return x in left..right && y >= top && y <= bottom
    }

    override fun draw(canvas: Canvas) {

        val x = translateX(trackedFace.boundingBox.centerX().toFloat())
        val y = translateY(trackedFace.boundingBox.centerY().toFloat())

        // Calculate positions.
        val left = (x - scale(trackedFace.boundingBox.width() / 2.0f))
        val top = (y - scale(trackedFace.boundingBox.height() / 2.0f))
        val right = (x + scale(trackedFace.boundingBox.width() / 2.0f))
        val bottom = (y + scale(trackedFace.boundingBox.height() / 2.0f))

        val lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH

        var yLabelOffset: Float = -lineHeight

        // Decide color based on face ID
        val colorID = abs(trackedFace.id % NUM_COLORS)

        // Calculate width and height of label box

        yLabelOffset -= 3 * lineHeight

        // Draw labels
        canvas.drawRect(
            left - BOX_STROKE_WIDTH,
            top + yLabelOffset,
            left + 2 * BOX_STROKE_WIDTH,
            top,
            labelPaints[colorID]
        )
        yLabelOffset += ID_TEXT_SIZE
        canvas.drawRect(left, top, right, bottom, boxPaints[colorID])
        canvas.drawText(
            trackedFace.displayName,
            left,
            top + yLabelOffset,
            idPaints[colorID]
        )
        yLabelOffset += lineHeight
    }

    companion object {
        private const val ID_TEXT_SIZE = 20.0f
        private const val BOX_STROKE_WIDTH = 5.0f
        private const val NUM_COLORS = 10
        private val COLORS = arrayOf(
            intArrayOf(Color.BLACK, Color.WHITE),
            intArrayOf(Color.WHITE, Color.MAGENTA),
            intArrayOf(Color.BLACK, Color.LTGRAY),
            intArrayOf(Color.WHITE, Color.RED),
            intArrayOf(Color.WHITE, Color.BLUE),
            intArrayOf(Color.WHITE, Color.DKGRAY),
            intArrayOf(Color.BLACK, Color.CYAN),
            intArrayOf(Color.BLACK, Color.YELLOW),
            intArrayOf(Color.WHITE, Color.BLACK),
            intArrayOf(Color.BLACK, Color.GREEN)
        )
    }
}
