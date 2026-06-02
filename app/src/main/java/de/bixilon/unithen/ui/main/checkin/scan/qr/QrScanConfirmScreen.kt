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

package de.bixilon.unithen.ui.main.checkin.scan.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.graphql.util.CourseFetcher.ATTENDEES_FETCH_INTERVAL
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchEnrolled
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInUnknownUserException
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.ui.util.UiUtil.formatNow
import de.bixilon.unithen.ui.util.useAsyncNetwork
import okio.IOException
import java.util.*
import kotlin.time.Clock


@Composable
private fun Warning(confirming: Boolean, user: User?, enrolled: Boolean, attendee: Boolean, attempt: CheckInQueue?, message: String?) {
    val size = Modifier
        .height(200.dp)
        .width(200.dp)

    if (confirming) {
        return CircularProgressIndicator(modifier = size)
    }


    when {
        user == null -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
        !enrolled -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
        (attempt != null && attempt.message != null) || attendee -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
        attempt != null -> Icon(Icons.Filled.Warning, "", tint = Color.Yellow, modifier = size)

        else -> Icon(Icons.Filled.CheckCircle, "", tint = Color(0xFF00A000), modifier = size) // dark green
    }

    if (message != null || attempt?.message != null) {
        return ErrorBox(message ?: attempt?.message ?: "Error")
    }

    val warning = when {
        user == null -> "Unknown user!"
        !enrolled -> "User is not enrolled in course!"
        attempt != null && attempt.attempt == null -> "User is already checked in (checkout pending)"
        attendee -> "User is already checked in"
        attempt != null -> "User is already checked in (synchronization pending)"
        else -> null
    }

    warning?.let { ErrorBox(it) }
}

@Composable
private fun EnrolledListWarning(account: Account, course: Course) {
    val storage = LocalStorage.current

    if ((Clock.System.now() - course.fetched) < ATTENDEES_FETCH_INTERVAL) return

    var updating by remember { mutableStateOf(false) }
    val refresh = useAsyncNetwork<Unit>(account) {
        try {
            updating = true
            storage.fetchEnrolled(account, course, false)
        } finally {
            updating = false
        }
    }

    LaunchedEffect(Unit) { refresh.invoke(Unit) }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (updating) {
            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp)); Text("Updating enrolled list...")
        } else {
            Icon(Icons.Default.Warning, "", tint = Color.Yellow); Spacer(Modifier.width(16.dp)); Text("Enrolled list was last updated ${course.fetched.formatNow()}")
        }
    }
}


@Composable
fun QrScanConfirmScreen(user: User?, userId: UUID) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current
    val (account, course, appointment) = LocalScanContext.current

    val enrolled = user?.let { rememberStorage { users.isEnrolled(course, user) } } ?: false
    val attendee = rememberStorage { user?.let { users.isAttendee(appointment, user) } } ?: false
    val queue = rememberStorage { user?.let { checkInQueue[appointment, user] } }

    var message by remember { mutableStateOf<String?>(null) }
    var confirming by remember { mutableStateOf(false) }

    Screen(horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTitle(course.name)

        Warning(confirming, user, enrolled, attendee, queue, message)
        Spacer(Modifier.height(16.dp))


        if (user == null) {
            EnrolledListWarning(account, course)
            Spacer(Modifier.height(16.dp))
        }

        InfoContainer {
            user?.let { InfoPair("Name", "${user.firstname} ${user.lastname}") }
            InfoPair("Start", appointment.start.format())
            InfoPair("End", appointment.end.format())
            InfoPair("Location", appointment.location)
        }

        Spacer(Modifier
            .weight(1.0f)
            .defaultMinSize(minHeight = 16.dp))

        val checkin = useAsyncNetwork<Unit>(account) {
            try {
                confirming = true
                if (user == null) {
                    CheckInUtil.checkIn(storage, account, appointment, userId)
                } else {
                    CheckInUtil.checkIn(storage, appointment, user)
                }

                navigation.pop()
            } catch (error: IOException) {
                if (user == null) {
                    message = "Network error"
                } else {
                    navigation.pop()
                }
                throw error
            } catch (error: CheckInUnknownUserException) {
                message = "Unknown user: " + (error.message ?: "")
            } catch (error: CheckInError) {
                message = "Error: " + (error.message ?: "")
            } finally {
                confirming = false
            }
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Button({
                navigation.pop()
            }, enabled = !confirming, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                Icon(Icons.Filled.Close, "cancel")
                Text("Cancel")
            }

            if (queue != null && queue.message == null) {
                Button({ checkin.invoke(Unit) }, enabled = !confirming, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onErrorContainer)) {
                    Icon(Icons.Filled.Sync, "synchronize")
                    Text("Try synchronize")
                }
            }

            Button({ checkin.invoke(Unit) }, enabled = message == null && !confirming && queue == null, modifier = Modifier.fillMaxWidth()) {
                if (user == null || !enrolled) { // TODO: danger button color?
                    Icon(Icons.Filled.Warning, "check")
                    Text("Try anyways")
                } else {
                    Icon(Icons.Filled.Check, "check")
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
fun QrScanConfirmScreen(userId: UUID) {
    val (_, _, appointment) = LocalScanContext.current

    val storage = LocalStorage.current
    val site = storage.sites[storage.courses[appointment.course]!!.site]!!
    val user = storage.users[site, userId]

    QrScanConfirmScreen(user, userId)
}
