package com.example.facex.ui.components.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Camera: ImageVector
	get() {
		if (_Camera != null) {
			return _Camera!!
		}
		_Camera = ImageVector.Builder(
            name = "Camera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
			path(
    			fill = SolidColor(Color.Black),
    			fillAlpha = 1.0f,
    			stroke = null,
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 1.0f,
    			strokeLineCap = StrokeCap.Butt,
    			strokeLineJoin = StrokeJoin.Miter,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(456f, 360f)
				horizontalLineToRelative(320f)
				quadToRelative(-27f, -69f, -82.5f, -118.5f)
				reflectiveQuadTo(566f, 172f)
				close()
				moveToRelative(-92f, 80f)
				lineToRelative(160f, -276f)
				quadToRelative(-11f, -2f, -22f, -3f)
				reflectiveQuadToRelative(-22f, -1f)
				quadToRelative(-66f, 0f, -123f, 25f)
				reflectiveQuadToRelative(-101f, 67f)
				close()
				moveTo(170f, 560f)
				horizontalLineToRelative(218f)
				lineTo(228f, 284f)
				quadToRelative(-32f, 41f, -50f, 90.5f)
				reflectiveQuadTo(160f, 480f)
				quadToRelative(0f, 21f, 2.5f, 40.5f)
				reflectiveQuadTo(170f, 560f)
				moveToRelative(224f, 228f)
				lineToRelative(108f, -188f)
				horizontalLineTo(184f)
				quadToRelative(27f, 69f, 82.5f, 118.5f)
				reflectiveQuadTo(394f, 788f)
				moveToRelative(86f, 12f)
				quadToRelative(66f, 0f, 123f, -25f)
				reflectiveQuadToRelative(101f, -67f)
				lineTo(596f, 520f)
				lineTo(436f, 796f)
				quadToRelative(11f, 2f, 21.5f, 3f)
				reflectiveQuadToRelative(22.5f, 1f)
				moveToRelative(252f, -124f)
				quadToRelative(32f, -41f, 50f, -90.5f)
				reflectiveQuadTo(800f, 480f)
				quadToRelative(0f, -21f, -2.5f, -40.5f)
				reflectiveQuadTo(790f, 400f)
				horizontalLineTo(572f)
				close()
				moveTo(480f, 880f)
				quadToRelative(-82f, 0f, -155f, -31.5f)
				reflectiveQuadToRelative(-127.5f, -86f)
				reflectiveQuadToRelative(-86f, -127.5f)
				reflectiveQuadTo(80f, 480f)
				quadToRelative(0f, -83f, 31.5f, -155.5f)
				reflectiveQuadToRelative(86f, -127f)
				reflectiveQuadToRelative(127.5f, -86f)
				reflectiveQuadTo(480f, 80f)
				quadToRelative(83f, 0f, 155.5f, 31.5f)
				reflectiveQuadToRelative(127f, 86f)
				reflectiveQuadToRelative(86f, 127f)
				reflectiveQuadTo(880f, 480f)
				quadToRelative(0f, 82f, -31.5f, 155f)
				reflectiveQuadToRelative(-86f, 127.5f)
				reflectiveQuadToRelative(-127f, 86f)
				reflectiveQuadTo(480f, 880f)
			}
		}.build()
		return _Camera!!
	}

private var _Camera: ImageVector? = null
