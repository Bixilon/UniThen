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

import androidx.compose.runtime.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import de.bixilon.unithen.ui.util.QrCameraPreview
import de.bixilon.unithen.util.json.Jackson
import java.util.*

data class ScannedQrCode(
    @field:JsonProperty("appointment_id") val appointmentId: UUID,
    @field:JsonProperty("user_id") val userId: UUID,
)

@Composable
fun QrScanAppointmentScreen() {
    val (_, _, appointment) = LocalScanContext.current
    var userId by remember { mutableStateOf<UUID?>(null) }

    if (userId != null) {
        QrScanConfirmScreen(userId!!)
        return
    }

    // TODO: overlay invalid qr codes
    // TODO: Show error message if scanned invalid for more than 1 second
    QrCameraPreview {
        try {
            val text = it.text ?: return@QrCameraPreview
            if (!text.startsWith("{")) return@QrCameraPreview


            val scanned = Jackson.MAPPER.readValue<ScannedQrCode>(text)

            if (scanned.appointmentId != appointment.uuid) return@QrCameraPreview

            userId = scanned.userId
        } catch (_: Throwable) {
        }
    }
}
