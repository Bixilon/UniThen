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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.R
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.main.AboutRoute
import de.bixilon.unithen.ui.main.AccountsRoute
import de.bixilon.unithen.ui.main.MainScreens
import de.bixilon.unithen.ui.main.settings.dialog.DatabaseCleanupDialog
import de.bixilon.unithen.ui.main.settings.types.BooleanSetting
import de.bixilon.unithen.ui.main.settings.types.EnumSetting
import de.bixilon.unithen.ui.main.settings.types.SettingsDialog
import de.bixilon.unithen.ui.main.settings.types.SettingsLink
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.i18n


@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()

    Screen(Modifier.verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle(R.string.settings_title.i18n())

        if (BuildConfig.DEBUG) {
            Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Debug")

                BooleanSetting(Settings.FAKE_TIME, "Debug: Fake time", "Only for appointment detection")
            }
            HorizontalDivider()
        }

        Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(R.string.settings_general.i18n())
            EnumSetting(Settings.ENTRYPOINT, MainScreens, R.string.settings_entrypoint.i18n(), R.string.settings_entrypoint_description.i18n())
        }
        HorizontalDivider()

        val tutor = rememberStorage { courses.isTutor() }
        if (tutor) {
            Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(R.string.settings_scan.i18n())
                BooleanSetting(Settings.SCAN_QR_HIGH_RESOLUTION, R.string.settings_scan_high_resolution.i18n(), R.string.settings_scan_high_resolution_description.i18n())
                BooleanSetting(Settings.SCAN_QR_AUTO_SCAN, R.string.settings_scan_auto_scan.i18n(), R.string.settings_scan_auto_scan_description.i18n())
                BooleanSetting(Settings.SCAN_AWAIT_SERVER_CONFIRMATION, R.string.settings_scan_await_server.i18n(), R.string.settings_scan_await_server_description.i18n())
                BooleanSetting(Settings.SCAN_CONFIRMATION_SCREEN, R.string.settings_scan_confirmation_screen.i18n(), R.string.settings_scan_confirmation_screen_description.i18n())
                BooleanSetting(Settings.SCAN_ALLOW_OFFLINE, R.string.settings_scan_offline.i18n(), R.string.settings_scan_offline_description.i18n())
            }
            HorizontalDivider()
        }

        Section(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(R.string.settings_advanced.i18n())
            BooleanSetting(Settings.QR_CODE_REMOVE_NAME, R.string.settings_advanced_remove_name.i18n(), R.string.settings_advanced_remove_name_description.i18n())
            BooleanSetting(Settings.FETCH_APPOINTMENTS, R.string.settings_advanced_fetch_appointments.i18n(), R.string.settings_advanced_fetch_appointments_description.i18n())
        }
        HorizontalDivider()

        SettingsLink(R.string.settings_accounts.i18n(), Icons.Default.AccountCircle, AccountsRoute)
        SettingsDialog(R.string.settings_cleanup_database.i18n(), Icons.Default.CleaningServices) { DatabaseCleanupDialog(it) }
        SettingsLink(R.string.settings_about.i18n(), Icons.Default.Info, AboutRoute)
    }
}
