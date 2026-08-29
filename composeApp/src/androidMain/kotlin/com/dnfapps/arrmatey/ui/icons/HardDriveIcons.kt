package com.dnfapps.arrmatey.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Hard_drive: ImageVector
    get() {
        if (_Hard_drive != null) return _Hard_drive!!

        _Hard_drive =
            ImageVector
                .Builder(
                    name = "Hard_drive",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 960f,
                    viewportHeight = 960f,
                ).apply {
                    path(
                        fill = SolidColor(Color(0xFF000000)),
                    ) {
                        moveTo(160f, 680f)
                        horizontalLineToRelative(640f)
                        verticalLineToRelative(-240f)
                        horizontalLineTo(160f)
                        close()
                        moveToRelative(520f, -60f)
                        quadToRelative(25f, 0f, 42.5f, -17.5f)
                        reflectiveQuadTo(740f, 560f)
                        reflectiveQuadToRelative(-17.5f, -42.5f)
                        reflectiveQuadTo(680f, 500f)
                        reflectiveQuadToRelative(-42.5f, 17.5f)
                        reflectiveQuadTo(620f, 560f)
                        reflectiveQuadToRelative(17.5f, 42.5f)
                        reflectiveQuadTo(680f, 620f)
                        moveToRelative(200f, -260f)
                        horizontalLineTo(767f)
                        lineToRelative(-80f, -80f)
                        horizontalLineTo(273f)
                        lineToRelative(-80f, 80f)
                        horizontalLineTo(80f)
                        lineToRelative(137f, -137f)
                        quadToRelative(11f, -11f, 25.5f, -17f)
                        reflectiveQuadToRelative(30.5f, -6f)
                        horizontalLineToRelative(414f)
                        quadToRelative(16f, 0f, 30.5f, 6f)
                        reflectiveQuadToRelative(25.5f, 17f)
                        close()
                        moveTo(160f, 760f)
                        quadToRelative(-33f, 0f, -56.5f, -23.5f)
                        reflectiveQuadTo(80f, 680f)
                        verticalLineToRelative(-320f)
                        horizontalLineToRelative(800f)
                        verticalLineToRelative(320f)
                        quadToRelative(0f, 33f, -23.5f, 56.5f)
                        reflectiveQuadTo(800f, 760f)
                        close()
                    }
                }.build()

        return _Hard_drive!!
    }

private var _Hard_drive: ImageVector? = null
