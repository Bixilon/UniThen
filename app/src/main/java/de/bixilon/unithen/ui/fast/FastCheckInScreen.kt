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

package de.bixilon.unithen.ui.fast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.exception.Broken
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.main.CheckInScreen
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.SimpleErrorScreen
import de.bixilon.unithen.ui.util.UiUtil.format
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant


@Preview
@Composable
fun FastCheckinNoAppointments() {
    SimpleErrorScreen("No upcoming courses!", "Are you sure you are there at the right time?")
}

@Composable
fun AppointmentCard(
    course: Course,
    appointment: Appointment,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${appointment.start.format()} - ${appointment.end.format()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun FastCheckinAppointmentSelector(appointments: List<Appointment>) {
    Column {
        Text(
            text = "Choose upcoming appointment",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(appointments, key = Appointment::id) { item ->
                val course by remember { DataStorage.STORAGE.courses.stateOf { this[item.course]!! } }
                val navigator = LocalNavigation.current
                AppointmentCard(course, item) { navigator.navigate(CheckInAppointment(course, item)) }
            }
        }
    }
}


@Composable
fun FastCheckinAccountSelector(course: Course, appointment: Appointment, accounts: List<Account>) {
    Column {
        Text(
            text = "Choose upcoming appointment",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts, key = Account::id) { item ->

                val navigation = LocalNavigation.current
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigation.navigate(CheckInRoute(item, course, appointment)) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = item.firstname + " " + item.lastname,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    Column {
        Text(course.name)
        Text(appointment.start.toString())
        Text("Choose an account for check in...")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts, key = Account::id) { item ->
                Card {
                    Text(item.firstname)
                    Text(item.lastname)
                }
            }
        }
    }
}

@Composable
fun FastCheckinAppointment(course: Course, appointment: Appointment) {
    val accounts by remember { DataStorage.STORAGE.accounts.stateOf { this[course] } }

    when (accounts.size) {
        0 -> Broken("Unassociated data left in database!")
        1 -> CheckInScreen(accounts[0], course, appointment)
        else -> FastCheckinAccountSelector(course, appointment, accounts)
    }
}

fun getTime(fake: Boolean) = if (fake) Instant.fromEpochSeconds(1769446901) else Clock.System.now()

@Composable
fun FastCheckInInScreen() {
    var fakeTime by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf(getTime(fakeTime)) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getTime(fakeTime)
            delay(10.seconds)
        }
    }


    val appointments by remember { DataStorage.STORAGE.appointments.stateOf { this.getInRange(time - 1.hours, time) } }


    if (BuildConfig.DEBUG) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = fakeTime,
                onCheckedChange = { fakeTime = it }
            )
            Text("Fake time")
        }
    }

    when (appointments.size) {
        0 -> FastCheckinNoAppointments()
        1 -> FastCheckinAppointment(remember { DataStorage.STORAGE.courses[appointments[0].course]!! }, appointments[0])
        else -> FastCheckinAppointmentSelector(appointments)
    }
}
