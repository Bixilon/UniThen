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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckIn
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.icons.QrCode
import de.bixilon.unithen.ui.main.ScanScanAppointmentRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage

@Composable
private fun AttendeeCard(appointment: Appointment, user: User) {
    val color = when {
        else -> MaterialTheme.colorScheme.primaryContainer
    }

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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AttemptCard(appointment: Appointment, attempt: CheckIn) {
    val storage = LocalStorage.current
    val user = storage.users[attempt.user]!!

    val color = when {
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnrolledCard(appointment: Appointment, user: User) {
    val color = when {
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
                text = user.firstName + " " + user.lastName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    // TODO: Check in checkbox
}

@Composable
fun ScanAppointmentScreen(appointment: Appointment) {
    val navigator = LocalNavigation.current
    val storage = LocalStorage.current

    val course = storage.courses[appointment.course]!!

    val attendees by remember { storage.users.stateOf { this.getAttendees(appointment) } }
    val pending by remember { storage.checkIns.stateOf { this.getNotOk(appointment) } }
    val enrolled by remember { storage.users.stateOf { this.getEnrolled(course) } }

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

        Button({ navigator.navigate(ScanScanAppointmentRoute(appointment)) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.QrCode, "scan")
            Text("Scan QR code")
        }


        Text(
            text = "Attendees (${attendees.size}/${enrolled.size})",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // TODO: PullToRefreshBox

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items = attendees, key = User::id) { AttendeeCard(appointment, it) }
            items(items = pending) { AttemptCard(appointment, it) } // TODO: provide key; remove attendees
            items(items = enrolled - attendees, key = User::id) { EnrolledCard(appointment, it) } // TODO: optimize in sql directly
        }
    }
}
