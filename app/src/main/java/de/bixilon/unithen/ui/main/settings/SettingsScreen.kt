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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.main.AboutRoute
import de.bixilon.unithen.ui.main.MainScreens
import de.bixilon.unithen.ui.main.settings.types.BooleanSetting
import de.bixilon.unithen.ui.main.settings.types.EnumSetting


@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier // TODO: Screen
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Settings")

        if (BuildConfig.DEBUG) {
            Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Debug")

                BooleanSetting(Settings.FAKE_TIME, "Debug: Fake time", "Only for appointment detection")
            }
            HorizontalDivider()
        }

        Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("General")
            EnumSetting(Settings.ENTRYPOINT, MainScreens, "Entrypoint", "Choose what screen should open when starting the app.")
        }
        HorizontalDivider()

        Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Check In")
            BooleanSetting(Settings.SCAN_QR_AUTO_SCAN, "Automatically scan", "Automatically starts QR code scanning")
        }
        HorizontalDivider()

        Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Advanced")
            BooleanSetting(Settings.QR_CODE_REMOVE_NAME, "Remove name (QR code)", "Remove name inside the QR code. This makes scanning the QR code easier. The name is not checked, however it might still break scaning.")
            BooleanSetting(Settings.SCAN_QR_HIGH_RESOLUTION, "High resolution scanning", "Prefers high resolution over faster QR code scanning. Enable if you have trouble scanning qr codes.")
        }
        HorizontalDivider()

        SettingsLink("About", Icons.Default.Info, AboutRoute)
    }
}
