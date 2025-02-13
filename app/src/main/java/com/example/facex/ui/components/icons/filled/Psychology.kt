package com.example.facex.ui.components.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Psychology: ImageVector
	get() {
		if (_Psychology != null) {
			return _Psychology!!
		}
		_Psychology = ImageVector.Builder(
            name = "Psychology",
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
				moveTo(240f, 880f)
				verticalLineToRelative(-172f)
				quadToRelative(-57f, -52f, -88.5f, -121.5f)
				reflectiveQuadTo(120f, 440f)
				quadToRelative(0f, -150f, 105f, -255f)
				reflectiveQuadToRelative(255f, -105f)
				quadToRelative(125f, 0f, 221.5f, 73.5f)
				reflectiveQuadTo(827f, 345f)
				lineToRelative(52f, 205f)
				quadToRelative(5f, 19f, -7f, 34.5f)
				reflectiveQuadTo(840f, 600f)
				horizontalLineToRelative(-80f)
				verticalLineToRelative(120f)
				quadToRelative(0f, 33f, -23.5f, 56.5f)
				reflectiveQuadTo(680f, 800f)
				horizontalLineToRelative(-80f)
				verticalLineToRelative(80f)
				horizontalLineToRelative(-80f)
				verticalLineToRelative(-160f)
				horizontalLineToRelative(160f)
				verticalLineToRelative(-200f)
				horizontalLineToRelative(108f)
				lineToRelative(-38f, -155f)
				quadToRelative(-23f, -91f, -98f, -148f)
				reflectiveQuadToRelative(-172f, -57f)
				quadToRelative(-116f, 0f, -198f, 81f)
				reflectiveQuadToRelative(-82f, 197f)
				quadToRelative(0f, 60f, 24.5f, 114f)
				reflectiveQuadToRelative(69.5f, 96f)
				lineToRelative(26f, 24f)
				verticalLineToRelative(208f)
				close()
				moveToRelative(200f, -280f)
				horizontalLineToRelative(80f)
				lineToRelative(6f, -50f)
				quadToRelative(8f, -3f, 14.5f, -7f)
				reflectiveQuadToRelative(11.5f, -9f)
				lineToRelative(46f, 20f)
				lineToRelative(40f, -68f)
				lineToRelative(-40f, -30f)
				quadToRelative(2f, -8f, 2f, -16f)
				reflectiveQuadToRelative(-2f, -16f)
				lineToRelative(40f, -30f)
				lineToRelative(-40f, -68f)
				lineToRelative(-46f, 20f)
				quadToRelative(-5f, -5f, -11.5f, -9f)
				reflectiveQuadToRelative(-14.5f, -7f)
				lineToRelative(-6f, -50f)
				horizontalLineToRelative(-80f)
				lineToRelative(-6f, 50f)
				quadToRelative(-8f, 3f, -14.5f, 7f)
				reflectiveQuadToRelative(-11.5f, 9f)
				lineToRelative(-46f, -20f)
				lineToRelative(-40f, 68f)
				lineToRelative(40f, 30f)
				quadToRelative(-2f, 8f, -2f, 16f)
				reflectiveQuadToRelative(2f, 16f)
				lineToRelative(-40f, 30f)
				lineToRelative(40f, 68f)
				lineToRelative(46f, -20f)
				quadToRelative(5f, 5f, 11.5f, 9f)
				reflectiveQuadToRelative(14.5f, 7f)
				close()
				moveToRelative(40f, -100f)
				quadToRelative(-25f, 0f, -42.5f, -17.5f)
				reflectiveQuadTo(420f, 440f)
				reflectiveQuadToRelative(17.5f, -42.5f)
				reflectiveQuadTo(480f, 380f)
				reflectiveQuadToRelative(42.5f, 17.5f)
				reflectiveQuadTo(540f, 440f)
				reflectiveQuadToRelative(-17.5f, 42.5f)
				reflectiveQuadTo(480f, 500f)
			}
		}.build()
		return _Psychology!!
	}

private var _Psychology: ImageVector? = null
