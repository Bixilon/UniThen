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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.readValue
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.QrCameraPreview
import de.bixilon.unithen.util.json.Jackson
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class ScannedQrCode(
    @field:JsonProperty("appointment_id") val appointmentId: UUID,
    @field:JsonProperty("user_id") val userId: UUID,
)

data class ErrorResult(
    val message: String,
) {
    val time = Clock.System.now()
}

fun getInvalidReason(storage: SqlStorage, course: Course, appointment: Appointment, userId: UUID): String? {
    val site = storage.sites[storage.courses[appointment.course]!!.site]!!
    val user = storage.users[site, userId] ?: return "Unknown user"


    val enrolled = storage.users.isEnrolled(course, user)
    if (!enrolled) return "Not enrolled"
    val attempt = storage.checkInAttempts[appointment, user]
    if (attempt != null) return "Already checked in"

    return null
}

@Composable
private fun ScanInstructions(course: Course?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 50.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {

            if (course != null) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Text("Please scan the QR code")
        }
    }
}

@Composable
private fun ErrorOverlay(errors: List<ErrorResult>) {
    if (errors.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(max = 300.dp)
            .padding(24.dp)
            .padding(bottom = 50.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (error in errors) {
                ErrorBox(error.message)
            }
        }
    }
}

@Composable
fun QrScanAppointmentScreen() {
    val storage = LocalStorage.current
    val (_, course, appointment) = LocalScanContext.current

    val errors = remember { mutableStateListOf<ErrorResult>() }

    var delayedUserId by remember { mutableStateOf<UUID?>(null) }

    var userId by remember { mutableStateOf<UUID?>(null) }

    if (userId != null) {
        QrScanConfirmScreen(userId!!)
        return
    }

    LaunchedEffect(delayedUserId) {
        if (delayedUserId == null) return@LaunchedEffect
        delay(1.seconds)
        userId = delayedUserId
    }


    LaunchedEffect(Unit) {
        while (true) {
            val now = Clock.System.now()
            errors.removeIf { (now - it.time) > 1.seconds }
            delay(100.milliseconds)
        }
    }

    // TODO: overlay invalid qr codes

    Box(modifier = Modifier.fillMaxSize()) {
        QrCameraPreview {
            if (it.isNotEmpty()) {
                errors.clear()
            }
            for (code in it) {
                try {
                    val text = code.text ?: continue
                    if (!text.startsWith("{")) {
                        errors += ErrorResult("Invalid QR code format!")
                        continue
                    }


                    val scanned = Jackson.MAPPER.readValue<ScannedQrCode>(text)

                    if (scanned.appointmentId != appointment.uuid) {
                        errors += ErrorResult("Mismatching appointment (wrong course?)!")
                        continue
                    }

                    val result = getInvalidReason(storage, course, appointment, scanned.userId)


                    if (result == null) {
                        userId = scanned.userId
                        delayedUserId = null
                    } else {
                        errors += ErrorResult(result)
                        delayedUserId = scanned.userId
                    }
                } catch (_: JacksonException) {
                    errors += ErrorResult("Invalid QR code data!")
                } catch (error: Throwable) {
                    errors += ErrorResult("Error: " + (error.message ?: ""))
                }
            }
        }
    }

    ScanInstructions(course)

    ErrorOverlay(errors)
}
