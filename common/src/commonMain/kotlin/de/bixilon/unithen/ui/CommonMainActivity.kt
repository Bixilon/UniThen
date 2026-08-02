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

import androidx.compose.runtime.*
import de.bixilon.unithen.storage.sql.DummyStorage.initializeDummy
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.auth.AccountSyncScreen
import de.bixilon.unithen.ui.auth.LegacyWebviewAuthenticationScreen
import de.bixilon.unithen.ui.auth.ory.EmailAuthenticationScreen
import de.bixilon.unithen.ui.auth.ory.OryAuthenticationScreen
import de.bixilon.unithen.ui.auth.ory.OryOidcCallbackScreen
import de.bixilon.unithen.ui.auth.ory.OryOidcPrepareScreen
import de.bixilon.unithen.ui.containers.LoadingContainer
import de.bixilon.unithen.ui.error.CrashScreen
import de.bixilon.unithen.ui.icons.Logo
import de.bixilon.unithen.ui.main.*
import de.bixilon.unithen.ui.main.about.AboutScreen
import de.bixilon.unithen.ui.main.accounts.AccountDetailsScreen
import de.bixilon.unithen.ui.main.accounts.AccountsScreen
import de.bixilon.unithen.ui.main.add.AddAccountScreen
import de.bixilon.unithen.ui.main.checkin.present.PresentQrAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.present.PresentQrScreen
import de.bixilon.unithen.ui.main.checkin.scan.ScanAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanAnyScreen
import de.bixilon.unithen.ui.main.checkin.scan.qr.ScanQrAppointmentScreen
import de.bixilon.unithen.ui.main.checkin.scan.qr.confirm.ScanQrConfirmScreen
import de.bixilon.unithen.ui.main.courses.CourseDetailsScreen
import de.bixilon.unithen.ui.main.courses.CoursesScreen
import de.bixilon.unithen.ui.main.courses.appointments.AppointmentDetailsScreen
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.sync.rememberSyncEngine
import de.bixilon.unithen.ui.util.DelayedContent
import de.bixilon.unithen.ui.util.LocalUrlHandler
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.loading_database
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun AppointmentPopper(appointment: Appointment) {
    val navigator = LocalNavigation.current
    val time = useTime()

    LaunchedEffect(time) {
        if (appointment.canPerformCheckIn(time)) return@LaunchedEffect

        navigator.pop()
    }
}

@Composable
fun Navigator.MainNavigator() {

    Routes {
        composable<MainRoute> { MainScreen() }
        composable<AboutRoute> { AboutScreen() }


        composable<AccountsRoute> { AccountsScreen() }
        composable<AccountDetailsRoute> { AccountDetailsScreen(it.account) }

        composable<CoursesRoute> { CoursesScreen() }
        composable<CourseDetailsRoute> { CourseDetailsScreen(it.course) }
        composable<AppointmentDetailsRoute> { AppointmentDetailsScreen(it.appointment) }

        composable<PresentQrAppointmentRoute> {
            PresentQrAppointmentScreen(it.course, it.appointment)

            AppointmentPopper(it.appointment)
        }
        composable<PresentQrRoute> {
            PresentQrScreen(it.account, it.course, it.appointment)

            AppointmentPopper(it.appointment)
        }

        composable<ScanAppointmentRoute> {
            ScanAppointmentScreen(it.appointment)

            AppointmentPopper(it.appointment)
        }
        composable<ScanQrAppointmentRoute> {
            ScanQrAppointmentScreen(it.appointment)

            AppointmentPopper(it.appointment)
        }
        composable<ScanQrConfirmRoute> {
            ScanQrConfirmScreen(it.appointment, it.userId)

            AppointmentPopper(it.appointment)
        }

        composable<ScanAnyRoute> { QrScanAnyScreen() }


        composable<FeatureFlagRoute> { FeatureFlagScreen() }

        composable<CrashRoute> { CrashScreen(null, it.exception) }

        composable<AddAccountRoute> { AddAccountScreen() }
        composable<AuthenticationCallbackRoute> { AccountSyncScreen(it.site, it.authentication) }


        composable<AuthenticateRoute> { OryAuthenticationScreen(it.host) }
        composable<LegacyAuthenticationRoute> { LegacyWebviewAuthenticationScreen(it.host) }
        composable<EmailAuthenticationRoute> { EmailAuthenticationScreen(it.site, it.ory) }
        composable<OidcAuthenticationRoute> { OryOidcPrepareScreen(it.ory, it.provider) }
        composable<OidcAuthenticationCallbackRoute> { OryOidcCallbackScreen(it.flow, it.code) }
    }

    CompositionLocalProvider(
        LocalNavigation provides this,
    ) {
        Host()
        LocalUrlHandler()
    }
}

@Composable
fun Loader(content: @Composable () -> Unit) {
    val storage = LocalStorage.current

    var error by remember { mutableStateOf<Throwable?>(null) }
    var active by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                storage.helper.load()
                if (storage.accounts.count == 0) {
                    storage.initializeDummy()
                }
            }
        } catch (thrown: Throwable) {
            thrown.printStackTrace()
            error = thrown
        } finally {
            active = false
        }
    }

    if (active) {
        DelayedContent(100.milliseconds) {
            LoadingContainer(Res.string.loading_database.i18n(), Logo)
        }
        return
    }

    error?.let { CrashScreen("Error during database loading", it); return }

    content.invoke()
}

@Composable
fun CommonMainActivity() {
    Loader {
        val storage = LocalStorage.current
        val navigator = remember { Navigator(MainRoute) }
        val engine = rememberSyncEngine(storage, navigator)

        CompositionLocalProvider(
            LocalSyncEngine provides engine
        ) {
            navigator.MainNavigator()
        }
    }
}
