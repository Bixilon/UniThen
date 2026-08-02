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

package de.bixilon.unithen.ui.main.checkin.present

import de.bixilon.kutil.string.StringUtil.truncate
import de.bixilon.unithen.settings.QrVersion
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCodeV1
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCodeV2
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.Uuid

object QrEncoder {

    fun encodeV1(user: Uuid, appointment: Uuid, firstname: String, lastname: String): String {
        val node = JsonObject(mapOf(
            "appointment_id" to JsonPrimitive(appointment.toString()),
            "user_id" to JsonPrimitive(user.toString()),
            "userName" to JsonObject(mapOf(
                "last" to JsonPrimitive(lastname),
                "first" to JsonPrimitive(firstname),
            )),
        ))

        return node.toString()
    }

    fun encodeQr(version: QrVersion, account: Uuid, appointment: Uuid, firstname: String, lastname: String): String {
        return when (version) {
            QrVersion.V1 -> encodeV1(account, appointment, firstname.truncate(12), lastname.truncate(12))
            QrVersion.V1_NAMELESS -> ScannedQrCodeV1(appointment, account).encode()
            QrVersion.V2 -> ScannedQrCodeV2(appointment, account).encode()
        }
    }
}
