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

package de.bixilon.unithen.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.verticalScroll(
    state: LazyListState,
    width: Dp = 4.dp,
) = drawWithContent {
    drawContent()
    val indicator = state.scrollIndicatorState ?: return@drawWithContent


    val size = indicator.viewportSize
    val content = indicator.contentSize
    if (size >= indicator.contentSize) return@drawWithContent


    val height = ((size.toFloat() / content) * size)
    val offset = (indicator.scrollOffset.toFloat() / content) * size


    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(x = this.size.width  + 2.dp.toPx(), y = offset),
        alpha = 0.6f,
        size = Size(width = width.toPx(), height = maxOf(height, 16.dp.toPx())),
        cornerRadius = CornerRadius(4f)
    )
}

