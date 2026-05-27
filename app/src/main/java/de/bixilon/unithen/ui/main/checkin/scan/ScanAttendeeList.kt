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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.CheckInAttempt
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.ui.util.useAsyncNetwork
import java.util.*
import kotlin.time.Clock


@Composable
private fun AttendeeCard(attempt: CheckInAttempt) {
    val storage = LocalStorage.current
    val user = storage.users[attempt.user]!!

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstName + " " + user.lastName,
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstName + " " + user.lastName,
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
                Button({
                    val appointment = storage.appointments[attempt.appointment]!!
                    storage.checkInAttempts.update(appointment, user, uuid = UUID.randomUUID(), sync = Clock.System.now(), status = CheckInAttempt.Status.OK)
                }) { Icon(Icons.Filled.Check, "approve") }
            }
        }
    }
}

@Composable
private fun EnrolledCard(user: User) {
    LocalNavigation.current
    LocalContext.current
    val storage = LocalStorage.current

    var loading by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = user.firstName + " " + user.lastName,
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
fun ScanAttendeeList(refreshing: Boolean, refresh: (force: Boolean) -> Unit) {
    val storage = LocalStorage.current

    val (_, course, appointment) = LocalScanContext.current

    val attempts by remember { storage.checkInAttempts.stateOf { this[appointment] } }
    val enrolled by remember { storage.users.stateOf { this.getEnrolled(course) } }

    val ok = remember(attempts) { attempts.filter { it.status == CheckInAttempt.Status.OK } }
    val other = remember(attempts) { attempts.filter { it.status != CheckInAttempt.Status.OK } }

    // TODO: optimize in sql directly
    val not = remember(attempts, enrolled) { enrolled.filter { user -> attempts.none { it.user == user.id } } }



    Text(
        text = "Attendees (${ok.size}/${enrolled.size})",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )


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
