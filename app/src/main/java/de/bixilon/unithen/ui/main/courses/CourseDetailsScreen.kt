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
import de.bixilon.unithen.storage.*
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.fast.CheckInRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours


@Composable
private fun Header(site: Site, event: Event, course: Course, accounts: List<Account>) {
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
                text = "${site.name} (${site.url})",
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
        }
    }
}


@Composable
private fun Appointments(appointments: List<Appointment>, onSelect: (Appointment) -> Unit) {
    val state = rememberLazyListState()
    val now = remember { Clock.System.now() }

    LaunchedEffect(now, appointments) {
        val upcoming = appointments.indexOfLast { it.end >= now }
        if (upcoming < 0) return@LaunchedEffect

        val offset = snapshotFlow { state.layoutInfo.viewportEndOffset }.first { it > 0 } / 3

        state.animateScrollToItem(upcoming, -offset)
    }

    Text(
        text = "Appointments",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )


    LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = appointments, key = Appointment::id) { appointment ->
            val color = when {
                appointment.end < now -> MaterialTheme.colorScheme.surfaceContainerLow
                appointment.start <= now + 1.hours -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                onClick = { onSelect(appointment) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${appointment.start.format()} - ${appointment.end.format()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
fun CourseDetailsScreen(course: Course) {
    val storage = LocalStorage.current
    val event = remember { storage.events[course.event]!! }
    val site = remember { storage.sites[event.site]!! }
    val appointments by remember { storage.appointments.stateOf { this[course].sortedByDescending { it.start } } }
    val accounts by remember { storage.accounts.stateOf { this[course].sortedBy { it.lastname } } }


    Column(modifier = Modifier.padding(16.dp)) {
        Header(site, event, course, accounts)

        Spacer(modifier = Modifier.height(24.dp))


        val navigation = LocalNavigation.current
        Appointments(appointments) { navigation.navigate(CheckInRoute(accounts.first(), course, it)) }
    }
}
