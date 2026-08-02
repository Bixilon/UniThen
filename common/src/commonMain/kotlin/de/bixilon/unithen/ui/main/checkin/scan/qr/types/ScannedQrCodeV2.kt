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

import de.bixilon.unithen.ui.util.encoding.decodeAsBase45
import de.bixilon.unithen.ui.util.encoding.encodeBase45
import kotlin.uuid.Uuid

// This is not supported by UniNow, but it reduces the QR code size drastically.
data class ScannedQrCodeV2(
    val appointmentId: Uuid,
    val userId: Uuid,
) : ScannedQrCode {

    override fun encode(): String {
        val output = StringBuilder()
        output.append(MAGIC)
        output.append(appointmentId.toByteArray().encodeBase45())
        output.append(userId.toByteArray().encodeBase45())

        return output.toString()
    }

    companion object {
        const val MAGIC = "UTV2"

        fun decode(data: String): ScannedQrCodeV2? {
            if (data.length != 52) return null
            if (!data.startsWith(MAGIC)) return null

            val appointmentId = data.substring(4, 28).decodeAsBase45().let { Uuid.fromByteArray(it) }
            val userId = data.substring(28, 52).decodeAsBase45().let { Uuid.fromByteArray(it) }

            return ScannedQrCodeV2(appointmentId, userId)
        }
    }
}
