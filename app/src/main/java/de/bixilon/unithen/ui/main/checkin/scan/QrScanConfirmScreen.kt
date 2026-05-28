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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.CheckInAttempt
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.ui.util.useAsyncNetwork
import okio.IOException
import java.util.*


@Composable
fun QrScanConfirmScreen(user: User) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current
    val (account, course, appointment) = LocalScanContext.current

    val enrolled = storage.users.getEnrolled(course).find { it.id == user.id } != null // TODO: Optimize in sql
    val _attempt by remember { storage.checkInAttempts.stateOf { this[appointment, user] } }
    val attempt = _attempt

    var confirming by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                // TODO: Align left
                "Confirm",
                style = MaterialTheme.typography.headlineLarge,
            )

            val size = Modifier
                .height(200.dp)
                .width(200.dp)

            when {
                !enrolled -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
                confirming -> CircularProgressIndicator(modifier = size)
                attempt != null && attempt.status == CheckInAttempt.Status.FAILED -> Icon(Icons.Filled.Close, "", tint = Color.Red, modifier = size)
                attempt != null -> Icon(Icons.Filled.Warning, "", tint = Color.Yellow, modifier = size)

                else -> Icon(Icons.Filled.CheckCircle, "", tint = Color.Green, modifier = size)
            }

            val warning = when {
                !enrolled -> "User is not enrolled in course!"
                confirming -> null
                attempt != null && attempt.status == CheckInAttempt.Status.FAILED -> "Server did not accept previous check in (User might not be enrolled)!"
                attempt != null -> "User is already checked in${if (attempt.status == CheckInAttempt.Status.PENDING) " (synchronization pending)" else ""}!"
                else -> null
            }

            warning?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    tonalElevation = 2.dp,
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Course: ${course.name}", // TODO: bold
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Text(
                        text = "Name: ${account.firstname} ${account.lastname}", // TODO: bold
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Text(
                        text = "Start: ${appointment.start.format()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "End: ${appointment.end.format()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Location: ${appointment.location}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))


            val checkin = useAsyncNetwork<Unit>(account) {
                try {
                    val attempt = CheckInUtil.checkIn(storage, account, appointment, user)
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

            Button({
                confirming = true

                checkin.invoke(Unit)
            }, enabled = !confirming && enrolled && attempt == null, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Check, "check")
                Text("Confirm")
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

    if (user != null) {
        QrScanConfirmScreen(user)
        return
    }

    // TODO: Icon?

    SimpleErrorScreen("Unknown user!", "User was not found in local database. Perhaps refresh the attendees list?")
}
