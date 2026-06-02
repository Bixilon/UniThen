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

package de.bixilon.unithen.ui.main.checkin.scan.attendees

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.api.graphql.util.CourseFetcher.ATTENDEES_FETCH_INTERVAL
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchAttendees
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork
import de.bixilon.unithen.ui.util.verticalScroll
import java.util.*
import kotlin.time.Clock


@Composable
private fun AttendeeCard(user: User) {
    val storage = LocalStorage.current

    var loading by remember { mutableStateOf(false) } // TODO: Why? Isn't it moved to the queue instantly?

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstname + " " + user.lastname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // TODO: Show time (no data)
            }
            val (account, _, appointment) = LocalScanContext.current

            val checkout = useAsyncNetwork<Unit>(account) {
                try {
                    loading = true
                    CheckInUtil.checkOut(storage, appointment, user)
                } finally {
                    loading = false
                }
            }

            Checkbox(true, enabled = !loading, onCheckedChange = {
                if (loading) return@Checkbox
                checkout.invoke(Unit)
            })
        }
    }
}

@Composable
private fun AttemptCard(item: CheckInQueue) {
    val color = when {
        item.attempt != null -> MaterialTheme.colorScheme.secondaryContainer
        item.message != null -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val storage = LocalStorage.current
    val user = rememberStorage { users[item.user]!! }
    val appointment = rememberStorage { appointments[item.appointment]!! }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstname + " " + user.lastname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val text = when {
                    item.attempt != null -> "Checkout pending..."
                    item.message != null -> "Failed: " + item.message
                    else -> "Checkin pending..."
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                if (BuildConfig.DEBUG) {
                    IconButton({
                        storage.transaction {
                            storage.checkInQueue.delete(appointment, user)
                            storage.appointments.addAttendee(user, appointment, UUID.randomUUID())
                        }
                    }) { Icon(Icons.Filled.Check, "approve", tint = Color.Red) }
                }
                if (item.message != null) {
                    IconButton({
                        storage.checkInQueue.delete(appointment, user)
                    }) { Icon(Icons.Filled.Clear, "remove") }
                }
            }
        }
    }
}

@Composable
private fun EnrolledCard(user: User) {
    val storage = LocalStorage.current

    var loading by remember { mutableStateOf(false) } // TODO: Why? Isn't it moved to the queue instantly?

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstname + " " + user.lastname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            val (account, _, appointment) = LocalScanContext.current
            val checkin = useAsyncNetwork<Unit>(account) { CheckInUtil.checkIn(storage, appointment, user) }

            Checkbox(false, enabled = !loading, onCheckedChange = {
                if (loading) return@Checkbox
                loading = true
                checkin.invoke(Unit)
            })
        }
    }
}

@Composable
fun ScanAttendeeList() {
    val (account, course, appointment) = LocalScanContext.current

    val filter = rememberUserFilter()

    val enrolled = rememberStorage { users.getEnrolledCount(course) }

    val attendees = rememberStorage { users.getAttendees(appointment, filter.search, filter.sort, filter.order) }
    val queue = rememberStorage { checkInQueue[appointment, filter.search, filter.sort, filter.order] }

    val not = rememberStorage { users.getEnrolledNotCheckedIn(appointment, filter.search, filter.sort, filter.order) }


    val storage = LocalStorage.current
    var refreshing by remember { mutableStateOf(false) }
    val _refresh = useAsyncNetwork<Boolean>(account) {
        try {
            refreshing = true
            storage.fetchAttendees(account, appointment, it)
        } finally {
            refreshing = false
        }
    }

    fun refresh(force: Boolean) {
        if (refreshing) return
        _refresh.invoke(force)
    }

    LaunchedEffect(Unit) {
        if (appointment.attendeesFetched == null || Clock.System.now() - appointment.attendeesFetched < ATTENDEES_FETCH_INTERVAL) {
            refresh(false)
        }
    }


    Section {
        SectionTitle("Attendees (${attendees.size}/${enrolled})")

        val state = rememberLazyListState()

        LaunchedEffect(filter.search) { state.animateScrollToItem(0, 0) }

        UserFilterX(filter)

        PullToRefreshBox(refreshing, modifier = Modifier.fillMaxHeight(), onRefresh = { refresh(true) }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state),
                state = state,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items = attendees, key = User::id) { AttendeeCard(it) }
                items(items = queue, key = { it.user }) { AttemptCard(it) }
                items(items = not, key = User::id) { EnrolledCard(it) }
            }
        }
    }
}
