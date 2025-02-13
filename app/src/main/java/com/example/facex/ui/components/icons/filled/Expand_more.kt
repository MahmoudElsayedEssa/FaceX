package com.example.facex.ui.components.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ExpandMore: ImageVector
	get() {
		if (_ExpandMore != null) {
			return _ExpandMore!!
		}
		_ExpandMore = ImageVector.Builder(
            name = "Expand_more",
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
				moveTo(480f, 615f)
				lineTo(240f, 375f)
				lineToRelative(56f, -56f)
				lineToRelative(184f, 184f)
				lineToRelative(184f, -184f)
				lineToRelative(56f, 56f)
				close()
			}
		}.build()
		return _ExpandMore!!
	}

private var _ExpandMore: ImageVector? = null
