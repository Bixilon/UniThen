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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.R
import de.bixilon.unithen.UniThen
import de.bixilon.unithen.storage.DefaultStorage
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.error.CrashScreen
import de.bixilon.unithen.ui.icons.Logo
import de.bixilon.unithen.ui.main.*
import de.bixilon.unithen.ui.main.accounts.AccountDetailsScreen
import de.bixilon.unithen.ui.main.accounts.AccountsScreen
import de.bixilon.unithen.ui.main.add.AddAccountScreen
import de.bixilon.unithen.ui.main.checkin.present.PresentQrAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.present.PresentQrScreen
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.main.checkin.scan.ScanAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.scan.ScanContextValue
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanAnyScreen
import de.bixilon.unithen.ui.main.checkin.scan.qr.ScanQrAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.scan.qr.ScanQrConfirmScreen
import de.bixilon.unithen.ui.main.courses.CourseDetailsScreen
import de.bixilon.unithen.ui.main.courses.CoursesScreen
import de.bixilon.unithen.ui.main.courses.appointments.AppointmentDetailsScreen
import de.bixilon.unithen.ui.main.settings.SettingsScreen
import de.bixilon.unithen.ui.main.setup.SetupScreen
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.UniThenTheme
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useTime
import de.bixilon.unithen.util.AndroidUtil.activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


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
        composable<AppointmentDetailsRoute> { AppointmentDetailsScreen(it.appointment) }

        composable<PresentQrAppointmentRoute> {
            PresentQrAppointmentScreen(it.course, it.appointment)

            if (useTime() > it.appointment.end) {
                LocalNavigation.current.pop()
            }
        }
        composable<PresentQrRoute> {
            PresentQrScreen(it.account, it.course, it.appointment)

            if (useTime() > it.appointment.end) {
                LocalNavigation.current.pop()
            }
        }

        composable<ScanAppointmentRoute> {
            ScanAppointmentScreen(it.appointment)

            if (useTime() > (it.appointment.end + Appointment.CHECKIN_LATE_DURATION)) {
                LocalNavigation.current.pop()
            }
        }
        composable<ScanQrAppointmentRoute> {
            CompositionLocalProvider(
                LocalScanContext provides ScanContextValue(it.account, it.course, it.appointment),
            ) {
                ScanQrAppointmentScreen()
            }
            if (useTime() > (it.appointment.end + Appointment.CHECKIN_LATE_DURATION)) {
                LocalNavigation.current.pop()
            }
        }
        composable<ScanQrConfirmRoute> {
            CompositionLocalProvider(
                LocalScanContext provides ScanContextValue(it.account, it.course, it.appointment),
            ) {
                ScanQrConfirmScreen(it.userId)
            }
            if (useTime() > (it.appointment.end + Appointment.CHECKIN_LATE_DURATION)) {
                LocalNavigation.current.pop()
            }
        }

        composable<ScanAnyRoute> { QrScanAnyScreen() }


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

@Composable
fun Loader(content: @Composable () -> Unit) {
    val storage = LocalStorage.current

    var error by remember { mutableStateOf<Throwable?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var loader by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { // don't flash loader
        if (!loaded) {
            delay(100.milliseconds)
            loader = true
        }
    }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                storage.helper.writableDatabase
            } catch (thrown: Throwable) {
                error = thrown
            } finally {
                loaded = true
                loader = false
            }
        }
    }

    if (!loaded && loader) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp), contentAlignment = Alignment.TopCenter) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    Logo,
                    contentDescription = "logo",
                    modifier = Modifier
                        .size(300.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(16.dp))
                    Text(R.string.loading_database.i18n())
                }
            }
        }
        return
    }

    error?.let { CrashScreen("Error during database loading", it); return }

    if (!loaded) return

    LaunchedEffect(Unit) {
        if (storage.sites.count == 0) {
            // TODO: sync ui with this?
            CoroutineScope(Dispatchers.IO).launch { DefaultStorage.SITES.forEach { storage.sites.add(it) } }
        }
    }

    content.invoke()
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
                            Loader {
                                MainNavigator()
                            }
                        }
                    }
                }
            }
        }
    }
}
