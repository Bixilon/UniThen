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

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.verticalScroll(
    state: LazyListState,
    width: Dp = 4.dp,
) = verticalScrollbar(state, width, true)


@Composable
fun Modifier.verticalScrollbar(
    state: ScrollableState,
    width: Dp = 4.dp,
    absolute: Boolean = false,
    offset: Offset = Offset.Zero,
): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    return drawWithContent {
        drawContent()
        val indicator = state.scrollIndicatorState ?: return@drawWithContent


        val size = indicator.viewportSize
        val content = indicator.contentSize
        if (size >= indicator.contentSize) return@drawWithContent


        val height = ((size.toFloat() / content) * size)
        var y = (indicator.scrollOffset.toFloat() / content) * size

        if (!absolute) y += indicator.scrollOffset

        drawRoundRect(
            color = color,
            topLeft = Offset(x = this.size.width + 2.dp.toPx() + offset.x.dp.toPx(), y = y + offset.y.dp.toPx()),
            alpha = 0.6f,
            size = Size(width = width.toPx(), height = maxOf(height, 16.dp.toPx())),
            cornerRadius = CornerRadius(4f)
        )
    }
}

@Composable
fun Modifier.verticalScrollWithBar(state: ScrollState = rememberScrollState()) = verticalScroll(state).verticalScrollbar(state, offset = Offset(-8f, 0f))

