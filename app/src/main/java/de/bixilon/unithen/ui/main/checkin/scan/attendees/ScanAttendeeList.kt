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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.api.graphql.util.CourseFetcher.ATTENDEES_FETCH_INTERVAL
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchCheckInAttempts
import de.bixilon.unithen.storage.types.CheckInAttempt
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.ui.util.useAsyncNetwork
import java.util.*
import kotlin.time.Clock


@Composable
private fun AttendeeCard(attempt: CheckInAttempt) {
    val storage = LocalStorage.current
    val user = rememberStorage { users[attempt.user]!! }

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
                if (attempt.time != null) {
                    Text(
                        text = attempt.time.format(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val (_, _, appointment) = LocalScanContext.current
            Checkbox(true, enabled = false, onCheckedChange = { CheckInUtil.checkOut(storage, appointment, user) })
        }
    }
}

@Composable
private fun AttemptCard(attempt: CheckInAttempt) {
    val color = when (attempt.status) {
        CheckInAttempt.Status.FAILED -> MaterialTheme.colorScheme.errorContainer
        CheckInAttempt.Status.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
        else -> return
    }

    val storage = LocalStorage.current
    val user = storage.users[attempt.user]!!

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
                Text(
                    text = if (attempt.status == CheckInAttempt.Status.PENDING) "Synchronization pending..." else "Synchronization failed: " + (attempt.message ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (BuildConfig.DEBUG) {
                IconButton({
                    val appointment = storage.appointments[attempt.appointment]!!
                    storage.checkInAttempts.update(appointment, user, uuid = UUID.randomUUID(), sync = Clock.System.now(), status = CheckInAttempt.Status.OK)
                }) { Icon(Icons.Filled.Check, "approve", tint = Color.Red) }
            }
        }
    }
}

@Composable
private fun EnrolledCard(user: User) {
    val storage = LocalStorage.current

    var loading by remember { mutableStateOf(false) }

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
            val checkin = useAsyncNetwork<Unit>(account) { CheckInUtil.checkIn(storage, account, appointment, user) }
            Checkbox(false, enabled = !loading, onCheckedChange = {
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

    val attempts = rememberStorage { checkInAttempts[appointment, filter.search.value, filter.sort.value, filter.order.value] }
    val ok = remember(attempts) { attempts.filter { it.status == CheckInAttempt.Status.OK } }
    val other = remember(attempts) { attempts.filter { it.status != CheckInAttempt.Status.OK } }

    val not = rememberStorage { users.getEnrolledNotCheckedIn(appointment, course, filter.search.value, filter.sort.value, filter.order.value) }


    val storage = LocalStorage.current
    var refreshing by remember { mutableStateOf(false) }
    val _refresh = useAsyncNetwork<Boolean>(account) {
        try {
            refreshing = true
            storage.fetchCheckInAttempts(account, appointment, it)
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
        SectionTitle("Attendees (${ok.size}/${enrolled})")


        UserFilterX(filter)

        PullToRefreshBox(refreshing, modifier = Modifier.fillMaxHeight(), onRefresh = { refresh(true) }) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items = ok, key = { it.user }) { AttendeeCard(it) }
                items(items = other, key = { it.user }) { AttemptCard(it) }
                items(items = not, key = User::id) { EnrolledCard(it) }
            }
        }
    }
}
