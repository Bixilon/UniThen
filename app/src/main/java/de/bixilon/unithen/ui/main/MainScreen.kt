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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.fast.FastCheckInInScreen
import de.bixilon.unithen.ui.main.accounts.AccountsScreen
import de.bixilon.unithen.ui.main.checkin.scan.CheckInScreen
import de.bixilon.unithen.ui.main.courses.CoursesScreen
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.SettingsScreen
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.main.settings.types.Labeled
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.NavigationMode
import de.bixilon.unithen.ui.navigation.NavigationRoute
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage


enum class Destinations(
    val icon: ImageVector,
    override val label: String,
    val route: NavigationRoute,
) : Labeled {
    COURSES(Icons.Default.DateRange, "Courses", CoursesRoute),
    ACCOUNTS(Icons.Default.AccountCircle, "Accounts", AccountsRoute),
    CHECKIN_PRESENT(Icons.Default.QrCode, "Check In (Show)", CheckInPresentRoute), // TODO: Only show if enrolled in at least ine course
    CHECKIN_SCAN(Icons.Default.QrCodeScanner, "Check In (Scan)", CheckInScanRoute), // TODO: Only show if is tutor in at least on course
    SETTINGS(Icons.Default.Settings, "Settings", SettingsRoute),
    ;

    companion object : ValuesEnum<Destinations> {
        override val VALUES = values()
        override val NAME_MAP = names()
    }
}


@Composable
fun MainScreen() {
    val storage = LocalStorage.current
    var entrypoint by rememberSetting(Settings.ENTRYPOINT, Destinations)
    val navigator = remember { Navigator(entrypoint.route, NavigationMode.SINGLE) }
    val count by remember { storage.accounts.stateOf { count } }

    val _navigator = LocalNavigation.current
    LaunchedEffect(count) {
        if (count == 0) {
            _navigator.navigate(SetupRoute)
        }
    }


    navigator.routes {
        composable<CoursesRoute> { CoursesScreen() }
        composable<AccountsRoute> { AccountsScreen() }
        composable<SettingsRoute> { SettingsScreen() }
        composable<CheckInPresentRoute> { FastCheckInInScreen() }
        composable<CheckInScanRoute> { CheckInScreen() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1.0f)) { navigator.Host() }

        NavigationBar {
            Destinations.entries.forEach { destination ->
                NavigationBarItem(
                    selected = navigator.current().route == destination.route,
                    onClick = { navigator.navigate(destination.route) },
                    icon = { Icon(destination.icon, contentDescription = "") },
                    label = { Text(destination.label) }
                )
            }
        }
    }
}
