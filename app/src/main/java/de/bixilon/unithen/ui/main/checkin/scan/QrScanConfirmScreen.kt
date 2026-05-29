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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.CheckInAttempt
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.ui.util.useAsyncNetwork
import okio.IOException
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes


@Composable
fun QrScanConfirmScreen(user: User?, userId: UUID) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current
    val (account, course, appointment) = LocalScanContext.current

    val enrolled = user?.let { storage.users.isEnrolled(course, user) } ?: false
    val _attempt by remember { storage.checkInAttempts.stateOf { user?.let { this[appointment, user] } } }
    val attempt = _attempt

    var message by remember { mutableStateOf<String?>(null) }
    var confirming by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(course.name, style = MaterialTheme.typography.headlineLarge)

            val size = Modifier
                .height(200.dp)
                .width(200.dp)

            when {
                confirming -> CircularProgressIndicator(modifier = size)
                user == null -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
                !enrolled -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
                attempt != null && attempt.status == CheckInAttempt.Status.FAILED -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
                attempt != null -> Icon(Icons.Filled.Warning, "", tint = Color.Yellow, modifier = size)

                else -> Icon(Icons.Filled.CheckCircle, "", tint = Color(0xFF00A000), modifier = size) // dark green
            }

            val warning = when {
                confirming -> null
                user == null -> "Unknown user!" // TODO: Check if attendee list was fetched
                !enrolled -> "User is not enrolled in course!"
                attempt != null && attempt.status == CheckInAttempt.Status.FAILED -> "Server did not accept previous check in (User might not be enrolled)!"
                attempt != null -> "User is already checked in${if (attempt.status == CheckInAttempt.Status.PENDING) " (synchronization pending)" else ""}!"
                else -> null
            }

            (message ?: warning)?.let { ErrorBox(it) }

            if ((message != null || warning != null) && (Clock.System.now() - course.fetched) > 15.minutes) {
                ErrorBox("Potential outdated list", "The enrolled list was fetched over 15 minutes ago on ${course.fetched.format()}")
            }

            InfoContainer {
                user?.let { InfoPair("Name", "${user.firstname} ${user.lastname}") }
                InfoPair("Start", appointment.start.format())
                InfoPair("End", appointment.end.format())
                InfoPair("Location", appointment.location)
            }

            Spacer(Modifier.height(16.dp))


            useAsyncNetwork<Unit>(account) {
                try {
                    val attempt = CheckInUtil.checkIn(storage, account, appointment, user!!)
                    confirming = false
                    if (attempt.status == CheckInAttempt.Status.OK) {
                        navigation.pop()
                    }
                } catch (error: IOException) {
                    navigation.pop()
                    confirming = false
                    throw error
                } catch (error: Throwable) {
                    confirming = false
                    throw error
                }
            }
            val checkin = useAsyncNetwork<Unit>(account) {
                try {
                    val attempt = if (user == null) {
                        CheckInUtil.checkIn(storage, account, appointment, userId) // TODO: Show message?
                    } else {
                        CheckInUtil.checkIn(storage, account, appointment, user)
                    }
                    confirming = false
                    if (attempt == null) {
                        return@useAsyncNetwork // TODO: How can that happen
                    }
                    if (attempt.status == CheckInAttempt.Status.OK) {
                        navigation.pop()
                    }
                } catch (error: IOException) {
                    if (user == null) {
                        message = "Network error"
                    } else {
                        navigation.pop()
                    }
                    confirming = false
                    throw error
                } catch (error: CheckInUnknownUserException) {
                    message = "Unknown user: " + (error.message ?: "")
                    confirming = false
                } catch (error: Throwable) {
                    confirming = false
                    throw error
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button({
                    navigation.pop()
                }, enabled = !confirming, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                    Icon(Icons.Filled.Close, "cancel")
                    Text("Cancel")
                }

                if (attempt != null && attempt.status == CheckInAttempt.Status.PENDING) {
                    Button({
                        confirming = true
                        checkin.invoke(Unit)
                    }, enabled = !confirming, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onErrorContainer)) {
                        Icon(Icons.Filled.Sync, "synchronize")
                        Text("Try synchronize")
                    }
                }

                Button({
                    confirming = true
                    checkin.invoke(Unit)
                }, enabled = message == null && !confirming && attempt == null, modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
fun QrScanConfirmScreen(userId: UUID) {
    val (_, _, appointment) = LocalScanContext.current

    val storage = LocalStorage.current
    val site = storage.sites[storage.courses[appointment.course]!!.site]!!
    val user = storage.users[site, userId]

    QrScanConfirmScreen(user, userId)
}
