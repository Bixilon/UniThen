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

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Event
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.fast.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.ui.fast.PresentQrAppointmentRoute
import de.bixilon.unithen.ui.icons.QrCode
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.useTime


@Composable
private fun Header(site: Site, event: Event, course: Course, accounts: List<Account>) {
    val navigator = LocalNavigation.current
    val storage = LocalStorage.current

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "${site.name} (${site.host})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = event.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = accounts.joinToString(", ") { it.firstname + " " + it.lastname },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            val time = useTime()
            val _appointment by remember { storage.appointments.stateOf { this.getInRange(time, time + CHECKIN_EARLY_DURATION, canceled = false, member = true, tutor = false).find { it.course == course.id } } }
            val appointment = _appointment

            if (appointment != null) { // TODO: make button more beautiful
                Button({ navigator.navigate(PresentQrAppointmentRoute(course, appointment)) }) {
                    Icon(Icons.Default.QrCode, "checkin")
                }
            }
        }
    }
}

@Composable
fun CourseDetailsScreen(course: Course) {
    val storage = LocalStorage.current
    val event = remember { storage.events[course.event]!! }
    val site = remember { storage.sites[event.site]!! }
    val accounts by remember { storage.accounts.stateOf { this[course].sortedBy { it.lastname } } }


    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Header(site, event, course, accounts)

        Spacer(modifier = Modifier.height(12.dp))

        CourseAppointments(course)
        CourseEnrolled(course)
    }
}
