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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.ui.main.AboutRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation


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

@Composable
@Preview
fun SettingsScreen() {
    val navigator = LocalNavigation.current
    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
        )

        BooleanSetting(Settings.QR_CODE_FAKE_NAME, "Fake name (QR code)", "Replaces your name inside the QR code with \"Max Muster\". The name is not checked during checkin, the course leader can still see your actual name in the attendants list.")
        if (BuildConfig.DEBUG) {
            BooleanSetting(Settings.FAKE_TIME, "Debug: Fake time", "For appointment detection")
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navigator.navigate(AboutRoute) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "About", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.0f))
            Icon(Icons.Default.Info, contentDescription = "about", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
