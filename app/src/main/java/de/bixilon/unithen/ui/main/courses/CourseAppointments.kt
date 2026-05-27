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

package de.bixilon.unithen.ui.main.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours


@Composable
private fun AppointmentCard(appointment: Appointment) {
    val now = remember { Clock.System.now() }

    val color = when {
        appointment.canceled != null -> MaterialTheme.colorScheme.errorContainer
        appointment.end < now -> MaterialTheme.colorScheme.surfaceContainerLow
        appointment.start <= now + 1.hours -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${appointment.start.format()} - ${appointment.end.format()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (appointment.location.isNotBlank()) {
                Text(
                    text = appointment.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val storage = LocalStorage.current
            val tutors = remember { storage.users.getTutors(appointment) }
            if (tutors.isNotEmpty()) {
                Text(
                    text = tutors.joinToString(", ") { it.firstName + " " + it.lastName },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CourseAppointments(course: Course) {
    val storage = LocalStorage.current
    val appointments by remember { storage.appointments.stateOf { this[course].sortedByDescending { it.start } } }

    if (appointments.isEmpty()) return

    val state = rememberLazyListState()
    val now = remember { Clock.System.now() }

    LaunchedEffect(now, appointments) {
        val upcoming = appointments.indexOfLast { it.end >= now }
        if (upcoming < 0) return@LaunchedEffect

        val offset = snapshotFlow { state.layoutInfo.viewportEndOffset }.first { it > 0 } / 3

        state.animateScrollToItem(upcoming, -offset)
    }

    Text(
        text = "Appointments (+${appointments.filter { it.start > now && it.canceled == null }.size}/${appointments.size})",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )


    LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = appointments, key = Appointment::id) { appointment -> AppointmentCard(appointment) }
    }
}
