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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_LATE_DURATION
import de.bixilon.unithen.ui.components.qr.QrCameraPreview
import de.bixilon.unithen.ui.main.ScanQrConfirmRoute
import de.bixilon.unithen.ui.main.checkin.scan.qr.overlays.*
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCode
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCodeV1
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useHapticFeedback
import de.bixilon.unithen.ui.util.useTime


private fun List<AcceptedState>.canIgnore(scanned: ScannedQrCode) = when (scanned) {
    is ScannedQrCodeV1 -> any { it.result.appointment.uuid == scanned.appointmentId && it.result.user.uuid == scanned.userId }
}

@Composable
private fun QrScanScreen(appointments: List<Appointment>) {
    val navigation = LocalNavigation.current

    val haptic = useHapticFeedback()
    val storage = LocalStorage.current

    val accepted = rememberAcceptedStates()
    val errors = rememberErrorStates()
    var delayed by rememberDelayedOverlay()

    val auto by rememberSetting(Settings.SCAN_QR_AUTO_SCAN)
    val confirm by rememberSetting(Settings.SCAN_CONFIRMATION_SCREEN)

    Box(modifier = Modifier.fillMaxSize()) {
        QrCameraPreview(modifier = Modifier.fillMaxSize()) { codes ->
            if (codes.isNotEmpty()) {
                errors.clear()
            }
            for (code in codes) {
                val scanned = ScannedQrCode.decode(code.text)
                if (scanned == null) {
                    errors += ErrorState(QrScanResult.InvalidFormat)
                    continue
                }

                if (accepted.canIgnore(scanned)) continue

                val result = QrScanUtil.scan(storage, appointments, scanned)


                if (result is QrScanResult.Accepted) {
                    delayed = null
                    haptic.invoke(HapticFeedbackType.Confirm)
                    if (confirm) {
                        if (!auto) {
                            navigation.pop()
                        }
                        navigation.navigate(ScanQrConfirmRoute(result.appointment, result.user.uuid))
                        break
                    } else {
                        accepted += AcceptedState(result)
                        continue
                    }
                }
                if (result !is QrScanResult.Error) continue // crash?

                errors += ErrorState(result)

                if (result !is QrScanResult.SoftError) {
                    delayed = null
                    continue
                }

                delayed = result
            }
        }

        QrUpdateIndicator(Modifier.align(Alignment.TopEnd).padding(4.dp), appointments)
    }

    val courses = rememberStorage { appointments.map { storage.courses[it.course]!! }.toSet() }
    ScanInstructions(courses)

    ErrorOverlay(errors)
    AcceptedOverlay(accepted, courses.size > 1)
}

@Composable
fun ScanQrAppointmentScreen(appointment: Appointment) {
    QrScanScreen(listOf(appointment))
}

@Composable
fun QrScanAnyScreen() {
    val navigation = LocalNavigation.current
    val time = useTime()
    val appointments = rememberStorage { appointments.getInRange(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, tutor = true) }

    if (appointments.isEmpty()) {
        return navigation.pop()
    }

    QrScanScreen(appointments)
}
