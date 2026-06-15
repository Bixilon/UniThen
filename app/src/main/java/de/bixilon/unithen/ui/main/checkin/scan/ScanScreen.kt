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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_LATE_DURATION
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.ScanAnyRoute
import de.bixilon.unithen.ui.main.ScanAppointmentRoute
import de.bixilon.unithen.ui.main.checkin.present.AppointmentCard
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useTime

@Composable
private fun ChooseAppointment(appointments: List<Appointment>) {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current

    Screen {
        ScreenTitle(R.string.scan_choose_appointment_title.i18n())


        Box {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(appointments) {
                    val course = storage.courses[it.course]!!

                    AppointmentCard(course, it, Modifier.clickable { navigation.navigate(ScanAppointmentRoute(it)) })
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton({ navigation.navigate(ScanAnyRoute) }) {
                    Icon(Icons.Filled.QrCodeScanner, "scan")
                }
            }
        }
    }
}

@Composable
fun CheckInScanScreen() {
    var scanned by remember { mutableStateOf(false) }
    val navigation = LocalNavigation.current

    val autoScan by rememberSetting(Settings.SCAN_QR_AUTO_SCAN)
    if (autoScan) {
        LaunchedEffect(Unit) { if (!scanned) navigation.navigate(ScanAnyRoute); scanned = true }
    } else {
        LaunchedEffect(Unit) { scanned = true }
    }

    val time = useTime()

    val appointments = rememberStorage { appointments.getInRange(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, member = true, tutor = true) }


    when (appointments.size) {
        0 -> ScanNoAppointments()
        1 -> ScanAppointmentScreen(appointments.first())
        else -> ChooseAppointment(appointments)
    }
}
