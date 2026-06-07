/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */


package de.bixilon.unithen.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Logo: ImageVector
    get() {
        if (_Logo != null) return _Logo!!
        _Logo = ImageVector.Builder(
            name = "Logo",
            defaultWidth = 200.0.dp,
            defaultHeight = 200.0.dp,
            viewportWidth = 200.0f,
            viewportHeight = 200.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF3270B7)),
            ) {
                moveTo(24.0f, 0.0f)
                horizontalLineTo(176.0f)
                arcTo(24.0f, 24.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 200.0f, 24.0f)
                verticalLineTo(176.0f)
                arcTo(24.0f, 24.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 176.0f, 200.0f)
                horizontalLineTo(24.0f)
                arcTo(24.0f, 24.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.0f, 176.0f)
                verticalLineTo(24.0f)
                arcTo(24.0f, 24.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24.0f, 0.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                moveTo(30.0f, 30.0f)
                horizontalLineTo(70.0f)
                verticalLineTo(70.0f)
                horizontalLineTo(30.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                moveTo(130.0f, 30.0f)
                horizontalLineTo(170.0f)
                verticalLineTo(70.0f)
                horizontalLineTo(130.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                moveTo(30.0f, 130.0f)
                horizontalLineTo(70.0f)
                verticalLineTo(170.0f)
                horizontalLineTo(30.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3270B7)),
            ) {
                moveTo(42.0f, 42.0f)
                horizontalLineTo(58.0f)
                verticalLineTo(58.0f)
                horizontalLineTo(42.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3270B7)),
            ) {
                moveTo(142.0f, 42.0f)
                horizontalLineTo(158.0f)
                verticalLineTo(58.0f)
                horizontalLineTo(142.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3270B7)),
            ) {
                moveTo(42.0f, 142.0f)
                horizontalLineTo(58.0f)
                verticalLineTo(158.0f)
                horizontalLineTo(42.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                moveTo(100.0f, 78.0f)
                lineTo(65.0f, 95.0f)
                lineTo(100.0f, 112.0f)
                lineTo(135.0f, 95.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                moveTo(83.0f, 106.0f)
                lineTo(83.0f, 120.0f)
                quadTo(100.0f, 135.0f, 117.0f, 120.0f)
                lineTo(117.0f, 106.0f)
                lineTo(100.0f, 114.0f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2.0f,
            ) {
                moveTo(130.0f, 95.0f)
                lineTo(130.0f, 120.0f)
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                moveTo(133.0f, 118.0f)
                arcTo(3.0f, 3.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 130.0f, 121.0f)
                arcTo(3.0f, 3.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 127.0f, 118.0f)
                arcTo(3.0f, 3.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 130.0f, 115.0f)
                arcTo(3.0f, 3.0f, 0f, isMoreThanHalf = false, isPositiveArc = true, 133.0f, 118.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
            ) {
                moveTo(126.0f, 128.0f)
                lineTo(134.0f, 128.0f)
                lineTo(130.0f, 118.0f)
                close()
            }
        }.build()
        return _Logo!!
    }

private var _Logo: ImageVector? = null
