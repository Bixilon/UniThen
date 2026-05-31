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

package de.bixilon.unithen.ui.main.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.NavigationRoute

@Composable
fun SettingsLink(name: String, icon: ImageVector, route: NavigationRoute) {
    val navigator = LocalNavigation.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navigator.navigate(route) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.0f))

        Icon(icon, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
