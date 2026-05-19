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

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun getTime(fake: Boolean) = if (fake) Instant.fromEpochSeconds(1769446901) else Clock.System.now()

@Composable
fun FastCheckInInScreen() {
    val storage = LocalStorage.current
    var fakeTime by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf(getTime(fakeTime)) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getTime(fakeTime)
            delay(10.seconds)
        }
    }

    LaunchedEffect(fakeTime) { time = getTime(fakeTime) }


    val appointments by remember { storage.appointments.stateOf { this.getInRange(time, time + 1.hours + 30.minutes, canceled = false) } }


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
        1 -> FastCheckinAppointment(remember { storage.courses[appointments[0].course]!! }, appointments[0])
        else -> FastCheckinAppointmentSelector(appointments)
    }
}
