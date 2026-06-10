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

package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.unithen.R
import de.bixilon.unithen.ui.main.accounts.AccountsScreen
import de.bixilon.unithen.ui.main.checkin.present.CheckInPresentScreen
import de.bixilon.unithen.ui.main.checkin.scan.CheckInScanScreen
import de.bixilon.unithen.ui.main.courses.CoursesScreen
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.SettingsScreen
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.main.settings.types.Labeled
import de.bixilon.unithen.ui.main.setup.SetupScreen
import de.bixilon.unithen.ui.navigation.NavigationMode
import de.bixilon.unithen.ui.navigation.NavigationRoute
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.i18n


enum class MainScreens(
    val icon: ImageVector,
    override val label: Int,
    val route: NavigationRoute,
) : Labeled {
    COURSES(Icons.Default.DateRange, R.string.main_navigation_courses, CoursesRoute),
    ACCOUNTS(Icons.Default.AccountCircle, R.string.main_navigation_accounts, AccountsRoute),
    CHECKIN_PRESENT(Icons.Default.QrCode, R.string.main_navigation_checkin_present, CheckInPresentRoute),
    CHECKIN_SCAN(Icons.Default.QrCodeScanner, R.string.main_navigation_checkin_scan, CheckInScanRoute),
    SETTINGS(Icons.Default.Settings, R.string.main_navigation_settings, SettingsRoute),
    ;

    companion object : ValuesEnum<MainScreens> {
        override val VALUES = values()
        override val NAME_MAP = names()
    }
}

@Composable
fun ActualMainScreen() {
    val entrypoint by rememberSetting(Settings.ENTRYPOINT, MainScreens)
    val navigator = remember { Navigator(entrypoint.route, NavigationMode.SINGLE) }


    navigator.routes {
        composable<CoursesRoute> { CoursesScreen() }
        composable<AccountsRoute> { AccountsScreen() }
        composable<SettingsRoute> { SettingsScreen() }
        composable<CheckInPresentRoute> { CheckInPresentScreen() }
        composable<CheckInScanRoute> { CheckInScanScreen() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1.0f)) { navigator.Host() }

        NavigationBar {
            MainScreens.entries.forEach { destination ->
                val enabled = when (destination) {
                    MainScreens.CHECKIN_PRESENT -> rememberStorage { courses.isMember() }
                    MainScreens.CHECKIN_SCAN -> rememberStorage { courses.isTutor() }
                    else -> true
                }
                NavigationBarItem(
                    selected = navigator.current().route == destination.route,
                    onClick = { navigator.navigate(destination.route) },
                    icon = { Icon(destination.icon, contentDescription = "") },
                    label = { Text(destination.label.i18n(), textAlign = TextAlign.Center) },
                    enabled = enabled,
                )
            }
        }
    }
}


@Composable
fun MainScreen() {
    val accounts = rememberStorage { accounts.count }

    if (accounts == 0) {
        return SetupScreen()
    }

    ActualMainScreen()
}
