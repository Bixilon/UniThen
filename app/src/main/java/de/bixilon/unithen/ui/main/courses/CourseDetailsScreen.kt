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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetch
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_LATE_DURATION
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.Event
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.main.PresentQrAppointmentRoute
import de.bixilon.unithen.ui.main.ScanAppointmentRoute
import de.bixilon.unithen.ui.main.courses.appointments.CourseAppointments
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork
import de.bixilon.unithen.ui.util.useTime
import de.bixilon.unithen.ui.util.useToast


@Composable
private fun Header(site: Site, event: Event, course: Course, accounts: List<Account>) {

    InfoContainer {
        Text(
            text = course.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "${site.name} (${site.host})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = event.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = accounts.joinToString(", ") { it.fullname },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun CourseDetailsScreen(course: Course) {
    val storage = LocalStorage.current
    val navigator = LocalNavigation.current
    val event = rememberStorage { events[course.event]!! }
    val site = rememberStorage { sites[event.site]!! }
    val accounts = rememberStorage { accounts[course].sortedBy { it.lastname } }


    Box {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Header(site, event, course, accounts)

            val tutor = storage.accounts.getTutorAccount(course) ?: accounts.firstOrNull()
            val toast = useToast()
            val refresh = tutor?.let {
                useAsyncNetwork<Unit>(tutor) {
                    storage.fetch(tutor, course)

                    toast.invoke(R.string.courses_synchronize_done)
                }
            }

            PullToRefreshBox(refresh?.active ?: false, modifier = Modifier.fillMaxHeight(), onRefresh = { refresh?.invoke(Unit) }) {
                Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CourseAppointments(course)
                    CourseEnrolled(course)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(-15.dp, -15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val time = useTime()

            val present = rememberStorage { appointments.getInRange(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, member = true, tutor = false).find { it.course == course.id } }

            if (present != null) {
                FloatingActionButton({ navigator.navigate(PresentQrAppointmentRoute(course, present)) }) {
                    Icon(Icons.Filled.QrCode, "present")
                }
            }

            val scan = rememberStorage { appointments.getInRange(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, member = true, tutor = true).find { it.course == course.id } }
            if (scan != null) {
                FloatingActionButton({ navigator.navigate(ScanAppointmentRoute(scan)) }) {
                    Icon(Icons.Filled.QrCodeScanner, "scan")
                }
            }
        }
    }
}
