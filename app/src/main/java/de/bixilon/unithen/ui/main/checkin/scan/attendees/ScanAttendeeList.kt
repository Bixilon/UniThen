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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.R
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchAttendees
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchEnrolled
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.navigation.LocalVisibility
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.util.*
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid


@Composable
private fun AttendeeCard(user: User, readonly: Boolean) {
    val storage = LocalStorage.current
    val (account, _, appointment) = LocalScanContext.current

    val toast = useToast()
    val resources = LocalResources.current

    val checkout = useAsyncNetwork<Unit>(account) {
        try {
            CheckInUtil.checkOut(storage, appointment, user)
        } catch (error: CheckInError) {
            toast.invoke(resources.getString(R.string.scan_unknown_error_server, error.message ?: "Unknown error") + " (${user.fullname})")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.fullname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // TODO: Show time (missing data)
            }

            Checkbox(true, enabled = !readonly && !checkout.active, onCheckedChange = { checkout.invoke(Unit) })
        }
    }
}

@Composable
private fun QueueCard(item: CheckInQueue, readonly: Boolean) {
    val color = when {
        item.attempt != null -> MaterialTheme.colorScheme.surfaceContainer
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
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.fullname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val text = when {
                    item.attempt != null -> R.string.scan_queue_pending_checkout.i18n()
                    item.message != null -> R.string.scan_queue_failed.i18n(item.message)
                    else -> R.string.scan_queue_pending.i18n()
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
                            if (item.attempt == null) {
                                storage.appointments.addAttendee(user, appointment, Uuid.random())
                            }
                        }
                    }) { Icon(Icons.Filled.Check, "approve", tint = Color.Red) }
                }
                //  if (item.message == null) { // TODO: Remove that, there is no use for it.
                IconButton({
                    storage.checkInQueue.delete(appointment, user)
                }, enabled = !readonly) { Icon(Icons.Filled.Clear, "remove") }
                //     }
            }
        }
    }
}

@Composable
private fun EnrolledCard(user: User, readonly: Boolean) {
    val storage = LocalStorage.current

    val (account, _, appointment) = LocalScanContext.current
    val toast = useToast()
    val resources = LocalResources.current
    val checkin = useAsyncNetwork<Unit>(account) {
        try {
            CheckInUtil.checkIn(storage, appointment, user)
        } catch (error: CheckInError) {
            toast.invoke(resources.getString(R.string.scan_unknown_error_server, error.message ?: "Unknown error") + " (${user.fullname})")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.fullname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Checkbox(false, enabled = !readonly && !checkin.active, onCheckedChange = { checkin.invoke(Unit) })
        }
    }
}

@Composable
fun ScanAttendeeList() {
    val visible = LocalVisibility.current
    val (account, course, appointment) = LocalScanContext.current
    val scope = rememberCoroutineScope()

    val filter = rememberUserFilter()

    val enrolled = rememberStorage { users.getEnrolledCount(course) }

    val attendees = rememberStorageAsync { users.getAttendees(appointment, filter.search, filter.sort, filter.order) } ?: emptyList()
    val queue = rememberStorageAsync { checkInQueue[appointment, filter.search, filter.sort, filter.order] } ?: emptyList()

    val not = rememberStorageAsync { users.getEnrolledNotCheckedIn(appointment, filter.search, filter.sort, filter.order) } ?: emptyList()

    val state = rememberLazyListState()

    val storage = LocalStorage.current
    val refresh = useAsyncNetwork<Boolean>(account) {
        storage.fetchEnrolled(account, course, it)
        storage.fetchAttendees(account, appointment, it)

        if (appointment.fetchedAttendees == null) { // only on inital fetch
            scope.launch { state.animateScrollToItem(0, 0) }
        }
    }

    LaunchedEffect(Unit) {
        if (appointment.isAttendeesStale() || course.isEnrolledStale()) {
            refresh.invoke(false)
        }
    }

    if (!visible) { // TODO: This is not good, when checking out persons its weirdly scrolling down (to the key of that person)
        LaunchedEffect(attendees, queue, not) { state.animateScrollToItem(0, 0) }
    }


    Section {
        val count = remember(attendees, queue) { attendees.size + queue.filter { it.message == null && it.attempt == null }.size }
        SectionTitle(R.string.appointment_attendees_title.i18n(count, enrolled))


        LaunchedEffect(filter.search, filter.sort, filter.order) { state.animateScrollToItem(0, 0) }

        UserFilterX(filter)

        val time = useTime()
        val readonly = !appointment.canPerformCheckIn(time)

        PullToRefreshBox(refresh.active, modifier = Modifier.fillMaxHeight(), onRefresh = { refresh.invoke(true) }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state),
                state = state,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 150.dp),
            ) {
                items(items = attendees, key = User::id) { AttendeeCard(it, readonly) }
                items(items = queue, key = { it.user }) { QueueCard(it, readonly) }
                items(items = not, key = User::id) { EnrolledCard(it, readonly) }
            }
        }
    }
}
