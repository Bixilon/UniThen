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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.main.ScanScanAppointmentRoute
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.syncQueue
import de.bixilon.unithen.ui.main.checkin.scan.attendees.ScanAttendeeList
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.navigation.LocalVisibility
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useTime
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
private fun Sync(appointment: Appointment, pending: Int, onFinish: () -> Unit) {
    val storage = LocalStorage.current

    var done by remember { mutableIntStateOf(0) }

    val abort = remember { mutableStateOf(false) }

    LaunchedEffect(abort) { if (abort.value) onFinish() }
    DisposableEffect(abort) { onDispose { abort.value = true } }

    // TODO: Show errors
    LaunchedEffect(Unit) {
        while (true) {
            if (abort.value) break
            val item = storage.checkInQueue.take(appointment) ?: break
            done++

            try {
                syncQueue(storage, item)
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }

        onFinish()
    }

    BackHandler { abort.value = true }


    AlertDialog(
        confirmButton = {},
        dismissButton = { Button({ abort.value = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text("Cancel") } },
        onDismissRequest = { abort.value = true },
        icon = { Icon(Icons.Default.Sync, "") },
        title = { Text(stringResource(R.string.scan_synchronizing_attendees)) },
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
fun CheckInAppointmentScreen(appointment: Appointment) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current

    val course = storage.courses[appointment.course]!!
    val account = storage.accounts.getTutorAccount(course)


    if (account == null) {
        SimpleErrorScreen(stringResource(R.string.scan_no_account_message), stringResource(R.string.scan_no_account_title))
        return
    }

    val pending = rememberStorage { checkInQueue.getCount(appointment) }

    var syncing by remember { mutableStateOf(false) }

    val autoScan by rememberSetting(Settings.SCAN_QR_AUTO_SCAN)
    val visible = LocalVisibility.current

    LaunchedEffect(autoScan && visible) { if (autoScan && visible) navigation.navigate(ScanScanAppointmentRoute(account, course, appointment)) }


    if (syncing) {
        Sync(appointment, pending) { syncing = false }
    }


    Screen {
        ScreenTitle(course.name)

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
                var showSync by remember(Unit) { mutableStateOf(pending > 0) }

                LaunchedEffect(pending > 0) {
                    if (pending == 0) {
                        showSync = false
                    } else {
                        delay(2.seconds)
                        showSync = true
                    }
                }
                if (showSync && (useTime() - appointment.end) < Duration.ZERO) {
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
