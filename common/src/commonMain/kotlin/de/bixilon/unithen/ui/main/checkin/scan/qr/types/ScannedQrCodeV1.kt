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

package de.bixilon.unithen.ui.main.checkin.scan.qr.types

import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.util.Jackson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

sealed interface ScannedQrCode {

    companion object {

        fun decode(data: ByteArray): ScannedQrCode? {
            catchAll { ScannedQrCodeV1.decode(data) }?.let { return it }

            return null
        }
    }
}

@Serializable
data class ScannedQrCodeV1(
    @SerialName("appointment_id") val appointmentId: Uuid,
    @SerialName("user_id") val userId: Uuid,
) : ScannedQrCode {

    companion object {

        fun decode(data: ByteArray): ScannedQrCodeV1? {
            val text = data.decodeToString().trim()
            if (!text.startsWith("{")) {
                return null
            }

            return Jackson.MAPPER.decodeFromString<ScannedQrCodeV1>(text)
        }
    }
}
