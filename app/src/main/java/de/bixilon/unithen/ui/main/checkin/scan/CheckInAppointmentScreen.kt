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

package de.bixilon.unithen.ui.main.checkin.scan

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.graphql.util.CourseFetcher.ATTENDEES_FETCH_INTERVAL
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchCheckInAttempts
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.icons.QrCode
import de.bixilon.unithen.ui.main.ScanScanAppointmentRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork
import kotlin.time.Clock

@Composable
fun CheckInAppointmentScreen(appointment: Appointment) {
    val navigation = LocalNavigation.current
    LocalContext.current
    val storage = LocalStorage.current

    val course = storage.courses[appointment.course]!!
    val account = storage.accounts.get(course).firstOrNull() // TODO: get only tutor accounts

    if (account == null) {
        SimpleErrorScreen("No account", "No account who can perform check in?")
        return
    }


    var refreshing by remember { mutableStateOf(false) }


    val _refresh = useAsyncNetwork<Boolean>(account) { storage.fetchCheckInAttempts(account, appointment, it); refreshing = false }

    fun refresh(force: Boolean) {
        if (refreshing) return
        refreshing = true
        _refresh.invoke(force)
    }

    LaunchedEffect(Unit) {
        if (appointment.attendeesFetched == null || Clock.System.now() - appointment.attendeesFetched < ATTENDEES_FETCH_INTERVAL) {
            refresh(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            course.name,
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(Modifier.height(16.dp))

        Button({ navigation.navigate(ScanScanAppointmentRoute(account, course, appointment)) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.QrCode, "scan")
            Text("Scan QR code")
        }

        CompositionLocalProvider(
            LocalScanContext provides ScanContextValue(account, course, appointment),
        ) {
            ScanAttendeeList(refreshing, { refresh(it) })
        }
    }
}
