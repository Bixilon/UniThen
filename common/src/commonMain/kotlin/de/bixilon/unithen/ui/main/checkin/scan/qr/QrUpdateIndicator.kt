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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.graphql.util.CourseFetcher.MAX_PARALLEL_REQUESTS
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchAttendees
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

private val SEMAPHORE = Semaphore(MAX_PARALLEL_REQUESTS)

@Composable
fun QrUpdateIndicator(modifier: Modifier, appointments: List<Appointment>) {
    val storage = LocalStorage.current
    var progress by remember { mutableIntStateOf(-1) }
    var errored by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            errored = false
            val now = Clock.System.now()

            coroutineScope {
                appointments.mapNotNull {
                    if (!it.isAttendeesStale(now)) return@mapNotNull null
                    val account = storage.accounts.getTutorAccount(it) ?: return@mapNotNull null

                    if (progress < 0) {
                        progress = 0
                    }

                    async(Dispatchers.IO) {
                        SEMAPHORE.withPermit {
                            try {
                                storage.fetchAttendees(account, it, false)
                                progress++
                            } catch (error: Exception) {
                                error.printStackTrace()
                                errored = true
                            }
                        }
                    }
                }
            }.awaitAll()
            progress = -1

            delay(1.minutes)
        }
    }


    if (progress < 0) {
        if (errored) {
            val color = MaterialTheme.colorScheme.error
            Canvas(modifier = modifier.size(12.dp)) {
                drawCircle(color = color)
            }
        }
        return
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$progress/${appointments.size}")
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = if (errored) MaterialTheme.colorScheme.error else ProgressIndicatorDefaults.circularColor)
    }
}
