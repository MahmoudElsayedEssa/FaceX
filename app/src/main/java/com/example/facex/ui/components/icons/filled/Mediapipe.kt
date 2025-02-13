package com.example.facex.ui.components.icons.filled
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Mediapipe: ImageVector
	get() {
		if (_Mediapipe != null) {
			return _Mediapipe!!
		}
		_Mediapipe = ImageVector.Builder(
            name = "SimpleIconsMediapipe",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
			path(
    			fill = SolidColor(Color(0xFF000000)),
    			fillAlpha = 1.0f,
    			stroke = null,
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 1.0f,
    			strokeLineCap = StrokeCap.Butt,
    			strokeLineJoin = StrokeJoin.Miter,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(2.182f, 0f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.18f, 2.114f)
				lineTo(0f, 2.182f)
				verticalLineToRelative(6.545f)
				arcToRelative(2.182f, 2.182f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.364f, 0f)
				verticalLineTo(2.182f)
				arcTo(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.182f, 0f)
				moveToRelative(6.545f, 0f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.18f, 2.114f)
				lineToRelative(-0.002f, 0.068f)
				verticalLineToRelative(13.09f)
				arcToRelative(2.182f, 2.182f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.364f, 0f)
				verticalLineTo(2.183f)
				arcTo(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.727f, 0f)
				moveToRelative(6.546f, 0f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.182f, 2.182f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.182f, 2.182f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.182f, -2.182f)
				arcTo(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.273f, 0f)
				moveToRelative(6.545f, 0f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.18f, 2.114f)
				lineToRelative(-0.002f, 0.068f)
				verticalLineToRelative(19.636f)
				arcToRelative(2.182f, 2.182f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.364f, 0f)
				verticalLineTo(2.182f)
				arcTo(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21.818f, 0f)
				moveToRelative(-6.545f, 6.545f)
				curveToRelative(-1.1830f, 00f, -2.1450f, 0.940f, -2.1810f, 2.1140f)
				lineToRelative(-0.001f, 0.068f)
				verticalLineToRelative(13.091f)
				arcToRelative(2.182f, 2.182f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.364f, 0f)
				verticalLineTo(8.728f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.182f, -2.183f)
				moveTo(2.182f, 13.091f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.18f, 2.114f)
				lineTo(0f, 15.273f)
				verticalLineToRelative(6.545f)
				arcToRelative(2.182f, 2.182f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.364f, 0f)
				verticalLineToRelative(-6.545f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.182f, -2.182f)
				moveToRelative(6.545f, 6.545f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.182f, 2.182f)
				arcTo(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.727f, 24f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.182f, -2.182f)
				arcToRelative(2.18f, 2.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.182f, -2.182f)
			}
		}.build()
		return _Mediapipe!!
	}


private var _Mediapipe: ImageVector? = null
