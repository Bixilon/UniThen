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

import android.accounts.NetworkErrorException
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.api.graphql.util.CourseFetcher.ATTENDEES_FETCH_INTERVAL
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchCheckInAttempts
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckIn
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.icons.QrCode
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.main.ScanScanAppointmentRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock

@Composable
private fun AttendeeCard(appointment: Appointment, user: User) {
    val storage = LocalStorage.current
    val attempt = storage.checkIns.get(appointment, user)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstName + " " + user.lastName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (attempt != null) {
                    Text(
                        text = attempt.time.format(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Checkbox(true, onCheckedChange = { CheckInUtil.checkOut(storage, appointment, user) })
        }
    }
}

@Composable
private fun AttemptCard(attempt: CheckIn) {
    val color = when (attempt.status) {
        CheckIn.Status.FAILED -> MaterialTheme.colorScheme.errorContainer
        CheckIn.Status.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
        else -> return
    }

    val storage = LocalStorage.current
    val user = storage.users[attempt.user]!!

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = user.firstName + " " + user.lastName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (attempt.status == CheckIn.Status.PENDING) "Synchronization pending..." else "Synchronization failed: " + (attempt.message ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnrolledCard(appointment: Appointment, user: User) {
    val storage = LocalStorage.current

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstName + " " + user.lastName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Checkbox(false, onCheckedChange = { CheckInUtil.checkIn(storage, appointment, user) })
        }
    }
}

@Composable
fun ScanAppointmentScreen(appointment: Appointment) {
    val navigation = LocalNavigation.current
    val context = LocalContext.current
    val storage = LocalStorage.current

    val course = storage.courses[appointment.course]!!
    val account = storage.accounts.get(course).firstOrNull() // TODO: get only tutor

    if (account == null) {
        SimpleErrorScreen("No account", "No account who can perform check in?")
        return
    }


    var refreshing by remember { mutableStateOf(false) }

    val attendees by remember { storage.users.stateOf { this.getAttendees(appointment) } }
    val pending by remember { storage.checkIns.stateOf { this.getNotOk(appointment) } }
    val enrolled by remember { storage.users.stateOf { this.getEnrolled(course) } }


    fun refresh(force: Boolean) {
        if (refreshing) return
        refreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                storage.fetchCheckInAttempts(account, appointment, force)

            } catch (_: AuthenticationException) {
                storage.accounts.logout(account)
                withContext(Dispatchers.Main) { Toast.makeText(context, "Please reauthenticate!", Toast.LENGTH_SHORT).show() }
                navigation.navigate(ReauthenticateRoute(storage.sites[account.site]!!))
            } catch (error: NetworkErrorException) { // TODO: correct exception
                withContext(Dispatchers.Main) { Toast.makeText(context, "Network error!", Toast.LENGTH_SHORT).show() }
            } catch (error: Throwable) {
                error.printStackTrace()
                navigation.navigate(CrashRoute(error))
            }
            withContext(Dispatchers.Main) { refreshing = false }
        }
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

        Button({ navigation.navigate(ScanScanAppointmentRoute(appointment)) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.QrCode, "scan")
            Text("Scan QR code")
        }


        Text(
            text = "Attendees (${attendees.size}/${enrolled.size})",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        PullToRefreshBox(refreshing, modifier = Modifier.weight(1.0f), onRefresh = { refresh(true) }) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items = attendees, key = User::id) { AttendeeCard(appointment, it) }
                items(items = pending) { AttemptCard(it) } // TODO: provide key; remove attendees
                items(items = enrolled - attendees, key = User::id) { EnrolledCard(appointment, it) } // TODO: optimize in sql directly
            }
        }
    }
}
