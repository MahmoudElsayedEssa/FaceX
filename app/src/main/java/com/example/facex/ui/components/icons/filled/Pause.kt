package com.example.facex.ui.components.icons.filled


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

 val Pause: ImageVector
    get() {
        if (_Pause != null) {
            return _Pause!!
        }
        _Pause = Builder(name = "Pause", defaultWidth = 512.0.dp, defaultHeight = 512.0.dp,
            viewportWidth = 512.0f, viewportHeight = 512.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(107.6f, 1.6f)
                curveToRelative(-25.3f, 5.7f, -44.5f, 22.9f, -53.3f, 47.9f)
                lineToRelative(-2.8f, 8.0f)
                lineToRelative(0.0f, 198.5f)
                lineToRelative(0.0f, 198.5f)
                lineToRelative(2.8f, 8.0f)
                curveToRelative(11.1f, 31.6f, 40.0f, 51.3f, 72.3f, 49.2f)
                curveToRelative(18.0f, -1.2f, 32.5f, -7.9f, 46.0f, -21.2f)
                curveToRelative(6.4f, -6.4f, 9.0f, -9.8f, 12.6f, -17.2f)
                curveToRelative(9.0f, -17.9f, 8.3f, 0.7f, 8.3f, -217.3f)
                curveToRelative(0.0f, -218.0f, 0.7f, -199.4f, -8.3f, -217.3f)
                curveToRelative(-8.1f, -16.3f, -23.9f, -29.6f, -41.7f, -35.3f)
                curveToRelative(-10.4f, -3.3f, -25.9f, -4.1f, -35.9f, -1.8f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(377.4f, 1.0f)
                curveToRelative(-14.3f, 2.6f, -26.5f, 9.2f, -38.0f, 20.5f)
                curveToRelative(-6.4f, 6.4f, -8.9f, 9.8f, -12.6f, 17.2f)
                curveToRelative(-9.0f, 17.9f, -8.3f, -0.7f, -8.3f, 217.3f)
                curveToRelative(0.0f, 218.0f, -0.7f, 199.4f, 8.3f, 217.3f)
                curveToRelative(8.1f, 16.3f, 23.7f, 29.5f, 41.7f, 35.4f)
                curveToRelative(6.8f, 2.2f, 9.8f, 2.6f, 20.5f, 2.7f)
                curveToRelative(14.1f, 0.1f, 21.1f, -1.4f, 32.1f, -6.8f)
                curveToRelative(17.4f, -8.6f, 29.8f, -22.8f, 36.6f, -42.1f)
                lineToRelative(2.8f, -8.0f)
                lineToRelative(0.0f, -198.5f)
                lineToRelative(0.0f, -198.5f)
                lineToRelative(-2.8f, -8.0f)
                curveToRelative(-8.4f, -23.6f, -26.2f, -40.7f, -49.2f, -47.0f)
                curveToRelative(-7.2f, -1.9f, -24.0f, -2.7f, -31.1f, -1.5f)
                close()
            }
        }.build()
        return _Pause!!
    }

private var _Pause: ImageVector? = null
