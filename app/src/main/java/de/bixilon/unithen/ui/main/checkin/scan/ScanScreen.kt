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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.fast.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.ui.fast.FastCheckinNoAppointments
import de.bixilon.unithen.ui.main.ScanAppointmentRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.ui.util.useTime

@Composable
fun AppointmentCard(appointment: Appointment) {
    val storage = LocalStorage.current
    val navigator = LocalNavigation.current
    val course = storage.courses[appointment.course]!!

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { navigator.navigate(ScanAppointmentRoute(appointment)) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${appointment.start.format()} - ${appointment.end.format()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (appointment.location.isNotBlank()) {
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChooseAppointment(appointments: List<Appointment>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            "Check in (${appointments.size}):",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(appointments) { AppointmentCard(it) }
        }
    }
}

@Composable
fun CheckInScreen() {
    val storage = LocalStorage.current

    val time = useTime()

    val appointments by remember { storage.appointments.stateOf { this.getInRange(time, time + CHECKIN_EARLY_DURATION, canceled = false, member = true, tutor = true) } }

    when (appointments.size) {
        0 -> FastCheckinNoAppointments() // TODO: proper error message
        1 -> CheckInAppointmentScreen(appointments.first())
        else -> ChooseAppointment(appointments)
    }
}
