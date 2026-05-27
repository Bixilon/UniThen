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

package de.bixilon.unithen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.UniThen
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.error.CrashScreen
import de.bixilon.unithen.ui.fast.PresentQrRoute
import de.bixilon.unithen.ui.main.*
import de.bixilon.unithen.ui.main.accounts.AccountDetailsScreen
import de.bixilon.unithen.ui.main.accounts.AccountsScreen
import de.bixilon.unithen.ui.main.add.AddAccountScreen
import de.bixilon.unithen.ui.main.checkin.present.CheckInPresentScreen
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.main.checkin.scan.QrScanAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.scan.ScanAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.scan.ScanContextValue
import de.bixilon.unithen.ui.main.courses.CourseDetailsScreen
import de.bixilon.unithen.ui.main.courses.CoursesScreen
import de.bixilon.unithen.ui.main.settings.SettingsScreen
import de.bixilon.unithen.ui.main.setup.SetupScreen
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.UniThenTheme
import de.bixilon.unithen.util.AndroidUtil.activity


@Composable
fun MainNavigator() {
    val navigator = remember { Navigator(if (BuildConfig.DEBUG) DebugRoute else MainRoute) }

    navigator.routes {
        composable<MainRoute> { MainScreen() }
        composable<DebugRoute> { DebugScreen() }
        composable<SetupRoute> { SetupScreen() }

        composable<AboutRoute> { AboutScreen() }


        composable<AccountsRoute> { AccountsScreen() }
        composable<AccountDetailsRoute> { AccountDetailsScreen(it.account) }

        composable<CoursesRoute> { CoursesScreen() }
        composable<CourseDetailsRoute> { CourseDetailsScreen(it.course) }

        composable<PresentQrRoute> { CheckInPresentScreen(it.account, it.course, it.appointment) }

        composable<ScanAppointmentRoute> { ScanAppointmentScreen(it.appointment) }
        composable<ScanScanAppointmentRoute> {
            CompositionLocalProvider(
                LocalScanContext provides ScanContextValue(it.account, it.course, it.appointment),
            ) {
                QrScanAppointmentScreen()
            }
        }


        composable<AddAccountRoute> { AddAccountScreen { navigator.pop() } }
        composable<ReauthenticateRoute> { AuthenticationScreen(it.site) { navigator.pop() } }

        composable<SettingsRoute> { SettingsScreen() }

        composable<CrashRoute> { CrashScreen(null, it.exception) }
    }

    CompositionLocalProvider(
        LocalNavigation provides navigator,
    ) {
        navigator.Host()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BackHandler { activity?.finish() }
            UniThenTheme {
                Scaffold(
                    modifier = Modifier.imePadding(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        CompositionLocalProvider(
                            LocalStorage provides UniThen.STORAGE,
                        ) {
                            MainNavigator()
                        }
                    }
                }
            }
        }
    }
}
