package com.example.facex.ui.components.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TensorFlow: ImageVector
    get() {
        if (_TensorFlow != null) {
            return _TensorFlow!!
        }
        _TensorFlow = ImageVector.Builder(
            name = "TensorFlow",
            defaultWidth = 128.dp,
            defaultHeight = 128.dp,
            viewportWidth = 128f,
            viewportHeight = 128f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFF6F00)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(61.55f, 128f)
                lineToRelative(-21.84f, -12.68f)
                verticalLineTo(40.55f)
                lineTo(6.81f, 59.56f)
                lineToRelative(0.08f, -28.32f)
                lineTo(61.55f, 0f)
                close()
                moveTo(66.46f, 0f)
                verticalLineToRelative(128f)
                lineToRelative(21.84f, -12.68f)
                verticalLineTo(79.31f)
                lineToRelative(16.49f, 9.53f)
                lineToRelative(-0.1f, -24.63f)
                lineToRelative(-16.39f, -9.36f)
                verticalLineToRelative(-14.3f)
                lineToRelative(32.89f, 19.01f)
                lineToRelative(-0.08f, -28.32f)
                close()
            }
        }.build()
        return _TensorFlow!!
    }

private var _TensorFlow: ImageVector? = null
