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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_LATE_DURATION
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.main.ScanQrConfirmRoute
import de.bixilon.unithen.ui.main.checkin.scan.LocalScanContext
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.QrCameraPreview
import de.bixilon.unithen.ui.util.useTime
import de.bixilon.unithen.util.Jackson
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@Serializable
data class ScannedQrCode(
    @SerialName("appointment_id") val appointmentId: Uuid,
    @SerialName("user_id") val userId: Uuid,
)

private data class AcceptedResult(
    val course: Course,
    val appointment: Appointment,
    val userId: Uuid,
)

data class ErrorResult(
    val reason: QrErrorReasons,
    val details: String? = null,
) {
    val time = Clock.System.now()
}

private fun getErrorReason(storage: SqlStorage, course: Course, appointment: Appointment, userId: Uuid): QrErrorReasons? {
    val site = storage.sites[storage.courses[appointment.course]!!.site]!!
    val user = storage.users[site, userId] ?: return QrErrorReasons.UNKNOWN_USER


    val enrolled = storage.users.isEnrolled(course, user)
    if (!enrolled) return QrErrorReasons.NOT_ENROLLED
    val attempt = storage.checkInQueue[appointment, user]
    if (attempt != null) return QrErrorReasons.ALREADY_CHECKED_IN

    return null
}

@Composable
private fun QrScanScreen(appointments: List<Appointment>) {
    val haptic = LocalHapticFeedback.current
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current

    val errors = remember { mutableStateListOf<ErrorResult>() }
    val delayedState = remember { mutableStateOf<AcceptedResult?>(null) }
    var delayed by delayedState

    LaunchedEffect(delayed) {
        val _delayed = delayed ?: return@LaunchedEffect
        delay(1.seconds)
        if (delayedState.value == _delayed) {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            navigation.navigate(ScanQrConfirmRoute(storage.accounts.getTutorAccount(_delayed.course)!!, _delayed.course, _delayed.appointment, _delayed.userId))
        }
        delayed = null
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
            if (it.isEmpty()) {
                delayed = null
            } else {
                errors.clear()
            }
            for (code in it) {
                try {
                    val text = code.text ?: continue
                    if (!text.startsWith("{")) {
                        errors += ErrorResult(QrErrorReasons.INVALID_FORMAT)
                        continue
                    }

                    val scanned = Jackson.MAPPER.decodeFromString<ScannedQrCode>(text)

                    val appointment = appointments.find { it.uuid == scanned.appointmentId }
                    if (appointment == null) {
                        errors += ErrorResult(QrErrorReasons.INVALID_APPOINTMENT, if (BuildConfig.DEBUG) scanned.appointmentId.toString() else null)
                        continue
                    }
                    val course = storage.courses[appointment.course]!!

                    val invalid = getErrorReason(storage, course, appointment, scanned.userId)

                    if (invalid == null) {
                        delayed = null
                        navigation.navigate(ScanQrConfirmRoute(storage.accounts.getTutorAccount(course)!!, course, appointment, scanned.userId))
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    } else {
                        errors += ErrorResult(invalid, if (BuildConfig.DEBUG) "User: ${scanned.userId}; Course: ${course.uuid}" else null)
                        delayed = AcceptedResult(course, appointment, scanned.userId)
                    }
                } catch (_: SerializationException) {
                    errors += ErrorResult(QrErrorReasons.INVALID_DATA)
                } catch (error: Throwable) {
                    errors += ErrorResult(QrErrorReasons.OTHER, error.message)
                }
            }
        }
    }

    val courses = rememberStorage { appointments.map { storage.courses[it.course]!! } }
    ScanInstructions(courses)

    ErrorOverlay(errors)
}

@Composable
fun ScanQrAppointmentScreen() {
    val (_, _, appointment) = LocalScanContext.current

    QrScanScreen(listOf(appointment))
}

@Composable
fun QrScanAnyScreen() {
    val time = useTime()
    val appointments = rememberStorage { appointments.getInRange(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, member = true, tutor = true) }

    QrScanScreen(appointments)
}
