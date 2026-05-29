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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.ui.main.AboutRoute
import de.bixilon.unithen.ui.main.Destinations
import de.bixilon.unithen.ui.main.settings.types.BooleanSetting
import de.bixilon.unithen.ui.main.settings.types.EnumSetting
import de.bixilon.unithen.ui.navigation.LocalNavigation


@Composable
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
        Text("Settings", style = MaterialTheme.typography.headlineLarge)

        if (BuildConfig.DEBUG) {
            Text("Debug", style = MaterialTheme.typography.headlineSmall)
            BooleanSetting(Settings.FAKE_TIME, "Debug: Fake time", "Only for appointment detection")
            HorizontalDivider()
        }


        Text("General", style = MaterialTheme.typography.headlineSmall)
        EnumSetting(Settings.ENTRYPOINT, Destinations, "Entrypoint", "Choose what screen should open when starting the app.")
        HorizontalDivider()


        Text("Advanced", style = MaterialTheme.typography.headlineSmall)
        BooleanSetting(Settings.QR_CODE_FAKE_NAME, "Fake name (QR code)", "Replaces your name inside the QR code with \"Max Muster\". The name is not checked during checkin, the course leader can still see your actual name in the attendants list.")
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
