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
import de.bixilon.unithen.R
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.ScanQrAppointmentRoute
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.SYNC_BACKOFF_NORMAL
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.syncQueue
import de.bixilon.unithen.ui.main.checkin.scan.attendees.ScanAttendeeList
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.TimeFormatUtil.format
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useTime
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
private fun SyncProgress(done: Int, pending: Int, dismiss: () -> Unit) {
    AlertDialog(
        confirmButton = {},
        dismissButton = { Button({ dismiss() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text("Hide") } },
        onDismissRequest = { dismiss() },
        icon = { Icon(Icons.Default.Sync, "") },
        title = { Text(R.string.scan_synchronizing_attendees.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("$done / $pending")
            }
        },
    )
}

@Composable
fun ScanAppointmentScreen(appointment: Appointment, info: Boolean = false) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current

    val course = storage.courses[appointment.course]!!
    val account = storage.accounts.getTutorAccount(course)


    if (account == null) {
        SimpleErrorScreen(R.string.scan_no_account_message.i18n(), R.string.scan_no_account_title.i18n())
        return
    }

    val time = useTime()
    val canSync = appointment.canSyncCheckIn(time)

    var synced by remember { mutableIntStateOf(0) }
    val pending = rememberStorage { checkInQueue.getCount(appointment) }

    var syncing by remember { mutableStateOf(false) }
    var forceSync by remember { mutableStateOf(false) }
    var showSyncProgress by remember { mutableStateOf(false) }


    if (canSync && pending > 0) {
        if (showSyncProgress) {
            SyncProgress(synced, pending) { showSyncProgress = false }
        }

        LaunchedEffect(syncing) {
            while (true) {
                val item = storage.checkInQueue.take(appointment, forceSync) ?: break
                synced++

                try {
                    syncQueue(storage, item)
                } catch (error: Throwable) {
                    error.printStackTrace()
                }
            }
            forceSync = false
            showSyncProgress = false
            syncing = false
            synced = 0
        }

        LaunchedEffect(Unit) {
            while (true) {
                syncing = true
                delay(SYNC_BACKOFF_NORMAL + 1.minutes) // additional minute (take does take the time when the sync started, and we'd always be a couple of seconds over it)
            }
        }
    }

    Screen {
        ScreenTitle(course.name)

        if (info) {
            InfoContainer {
                InfoPair(R.string.appointment_start.i18n(), appointment.start.format())
                InfoPair(R.string.appointment_end.i18n(), appointment.end.format())
                InfoPair(R.string.appointment_location.i18n(), appointment.location)
            }
        }

        Box {
            CompositionLocalProvider(
                LocalScanContext provides ScanContextValue(account, course, appointment),
            ) {
                ScanAttendeeList()
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (canSync) {
                    var showSyncButton by remember(Unit) { mutableStateOf(pending > 0) }

                    LaunchedEffect(pending > 0) {
                        if (pending == 0) {
                            showSyncButton = false
                        } else {
                            delay(2.seconds)
                            showSyncButton = true
                        }
                    }

                    if (showSyncButton) {
                        val color = if (syncing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        FloatingActionButton({ showSyncProgress = true; if (!syncing) forceSync = true; syncing = true }, containerColor = color) {
                            if (syncing) {
                                CircularProgressIndicator()
                            }
                            Icon(Icons.Filled.Sync, "sync")
                        }
                    }
                }
                if (appointment.canPerformCheckIn()) {
                    FloatingActionButton({ navigation.navigate(ScanQrAppointmentRoute(account, course, appointment)) }) {
                        Icon(Icons.Filled.QrCodeScanner, "scan")
                    }
                }
            }
        }
    }
}
