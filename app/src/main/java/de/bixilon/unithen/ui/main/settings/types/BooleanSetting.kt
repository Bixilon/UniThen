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

package de.bixilon.unithen.ui.main.settings.types

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.ui.main.settings.Setting
import de.bixilon.unithen.ui.main.settings.rememberSetting


@Composable
fun BooleanSetting(setting: Setting<Boolean>, title: String, description: String) {
    var value by rememberSetting(setting)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { value = !value },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier
            .weight(1.0f)
            .padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)

            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Switch(value, { value = it })
    }
}
