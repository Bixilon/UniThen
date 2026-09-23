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

package de.bixilon.unithen.ui.containers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.BackButton

@Composable
fun Screen(modifier: Modifier = Modifier, horizontalAlignment: Alignment.Horizontal = Alignment.Start, verticalArrangement: Arrangement.Vertical = Arrangement.Top, backButton: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    SafeBox {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 4.dp),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            content = content,
        )

        if (backButton) {
            BackButton()
        }
    }
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    val show = catchAll { LocalNavigation.current }?.takeIf { it.size > 1 } != null

    var modifier = modifier

    if (!show) {
        modifier = modifier.padding(bottom = 8.dp)
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (show) {
            BackButton()
        }

        Text(text = if (!show) "$text:" else text, style = MaterialTheme.typography.headlineLarge)
    }
}
