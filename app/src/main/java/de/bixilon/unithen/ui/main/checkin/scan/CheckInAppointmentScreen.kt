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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.graphql.util.CourseFetcher.ATTENDEES_FETCH_INTERVAL
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchCheckInAttempts
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.ScanScanAppointmentRoute
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.fetch
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@Composable
fun CheckInAppointmentScreen(appointment: Appointment) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current

    val course = storage.courses[appointment.course]!!
    val account = storage.accounts.getTutorAccount(course)


    if (account == null) {
        SimpleErrorScreen("No account", "No account who can perform check in?")
        return
    }

    val pending = rememberStorage { storage.checkInAttempts.getPendingSyncCount(appointment) }

    var refreshing by remember { mutableStateOf(false) }
    var syncing by remember { mutableStateOf(false) }


    val _refresh = useAsyncNetwork<Boolean>(account) {
        try {
            storage.fetchCheckInAttempts(account, appointment, it)
        } finally {
            refreshing = false
        }
    }

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


    if (syncing) {
        var done by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            val site = storage.sites[course.site]!!

            while (true) {
                val attempt = storage.checkInAttempts.takePendingSync(appointment) ?: break
                done++

                val user = storage.users[attempt.user]!!

                try {
                    fetch(storage, site, account, appointment, user)
                } catch (error: Throwable) {
                    done--
                    error.printStackTrace()
                }
            }

            syncing = false
        }


        AlertDialog(
            confirmButton = {},
            dismissButton = { Button({ syncing = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text("Cancel") } },
            onDismissRequest = { syncing = false },
            icon = { Icon(Icons.Default.Sync, "") },
            title = { Text("Synchronizing...") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("$done / $pending")
                }
            },
        )
    }


    Screen {
        ScreenTitle(course.name)

        Box {
            CompositionLocalProvider(
                LocalScanContext provides ScanContextValue(account, course, appointment),
            ) {
                ScanAttendeeList(refreshing) { refresh(it) }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var showSync by remember(Unit) { mutableStateOf(pending > 0) }

                LaunchedEffect(pending > 0) {
                    if (pending == 0) {
                        showSync = false
                    } else {
                        delay(2.seconds)
                        showSync = true
                    }
                }
                if (showSync) {
                    FloatingActionButton({ syncing = true }) {
                        Icon(Icons.Filled.Sync, "sync")
                    }
                }
                FloatingActionButton({ navigation.navigate(ScanScanAppointmentRoute(account, course, appointment)) }) {
                    Icon(Icons.Filled.QrCodeScanner, "scan")
                }
            }
        }
    }
}
