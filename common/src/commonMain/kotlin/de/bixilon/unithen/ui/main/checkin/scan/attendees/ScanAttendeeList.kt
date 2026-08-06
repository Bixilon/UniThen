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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.sync.SyncEngineCompleteEffect
import de.bixilon.unithen.ui.sync.useSyncEngine
import de.bixilon.unithen.ui.util.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.*
import kotlin.uuid.Uuid


@Composable
private fun AttendeeCard(modifier: Modifier, appointment: Appointment, user: User, readonly: Boolean) {
    val storage = LocalStorage.current

    val toast = useToast()

    val checkout = useAsyncNetwork {
        try {
            CheckInUtil.checkOut(storage, appointment, user)
        } catch (error: CheckInError) {
            toast.invoke(getString(Res.string.scan_error_rejected_message, error.error._i18n()) + " (${user.fullname})")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier,
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

            Checkbox(true, enabled = !readonly && !checkout.active, onCheckedChange = { checkout.invoke() })
        }
    }
}

@Composable
private fun QueueCard(modifier: Modifier, item: CheckInQueue, readonly: Boolean) {
    val sync = LocalSyncEngine.current
    val color = when {
        item.attempt != null -> MaterialTheme.colorScheme.surfaceContainer
        item.message != null -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val storage = LocalStorage.current
    val user = rememberStorage { users[item.user]!! }
    val appointment = rememberStorage { appointments[item.appointment]!! }

    val active = sync.isQueueActive(user, appointment)

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.fullname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val text = when {
                    item.attempt != null -> Res.string.scan_queue_pending_checkout.i18n()
                    item.error != null -> Res.string.scan_error_rejected_message.i18n(item.error!!.i18n())
                    else -> Res.string.scan_queue_pending.i18n()
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (active) {
                    CircularProgressIndicator()
                } else {
                    Icon(Icons.Filled.Warning, "pending")
                }
                if (RuntimeInfo.debug) {
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
private fun EnrolledCard(modifier: Modifier, appointment: Appointment, user: User, readonly: Boolean) {
    val storage = LocalStorage.current

    val toast = useToast()
    val checkin = useAsyncNetwork {
        try {
            CheckInUtil.checkIn(storage, appointment, user)
        } catch (error: CheckInError) {
            toast.invoke(getString(Res.string.scan_error_rejected_message, error.error._i18n()) + " (${user.fullname})")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.fullname,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Checkbox(false, enabled = !readonly && !checkin.active, onCheckedChange = { checkin.invoke() })
        }
    }
}

@Composable
fun ScanAttendeeList(appointment: Appointment) {
    val course = rememberStorage { courses[appointment.course]!! }
    val scope = rememberCoroutineScope()

    val filter = rememberUserFilter()

    val enrolled = rememberStorage { users.getEnrolledCount(course) }

    val (attendees, queue, not) = rememberStorageAsync(appointment, filter.search, filter.sort, filter.order) {
        val attendees = users.getAttendees(appointment, filter.search, filter.sort, filter.order)
        val queue = checkInQueue[appointment, filter.search, filter.sort, filter.order]
        val not = users.getEnrolledNotCheckedIn(appointment, filter.search, filter.sort, filter.order)

        return@rememberStorageAsync Triple(attendees, queue, not)
    } ?: Triple(emptyList(), emptyList(), emptyList())


    val state = rememberLazyListState()

    val synchronize = useSyncEngine {
        async { syncEnrolled(course) }
        async { syncAttendees(appointment) }
    }

    SyncEngineCompleteEffect(synchronize) {
        if (appointment.fetchedAttendees == null) { // only on inital fetch
            scope.launch { state.animateScrollToItem(0, 0) }
        }
    }

    LaunchedEffect(Unit) {
        if (appointment.isAttendeesStale() || course.isEnrolledStale()) {
            synchronize.invoke()
        }
    }


    Section {
        val count = remember(attendees, queue) { attendees.size + queue.filter { it.message == null && it.attempt == null }.size }
        SectionTitle(Res.string.appointment_attendees_title.i18n(count, enrolled))


        LaunchedEffect(filter.search, filter.sort, filter.order) { state.animateScrollToItem(0, 0) }

        UserFilterX(filter)

        val time = useTime()
        val readonly = !appointment.canPerformCheckIn(time)

        PullToRefreshBox(synchronize.active, modifier = Modifier.fillMaxHeight(), onRefresh = { synchronize.invoke(force = true) }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state),
                state = state,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 150.dp),
            ) {
                item("_") { Spacer(Modifier.height(1.dp)) } // https://stackoverflow.com/questions/74320761/compose-lazycolumn-key-messes-up-scrolling-when-sorting-the-items
                items(items = attendees, key = { it.id }) { AttendeeCard(modifier(), appointment, it, readonly) }
                items(items = queue, key = { it.user }) { QueueCard(modifier(), it, readonly) }
                items(items = not, key = { it.id }) { EnrolledCard(modifier(), appointment, it, readonly) }
            }
        }
    }
}


private fun LazyItemScope.modifier() = Modifier.fillMaxWidth().animateItem()
